#' @export
versionManager <- function(version="latest", check.update = TRUE) {

  is.offline <- class(
    try(curl::nslookup("amap-dev.cirad.fr"), silent = T))[1L] == "try-error"

  if (check.update && is.offline) {
    warning("Either offline or amap-dev.cirad.fr unreachable. Cannot check for update.",
            call. = FALSE, immediate. = TRUE)
    check.update <- FALSE
  }

  # list local versions
  localVersions <- getLocalVersions()
  # latest local version
  if (version == "latest") {
    version <- tail(localVersions$version, 1)
  }
  # valid version number
  stopifnot(is.validVersion(version))

  # short version number (major.minor, e.g. 1.7)
  if (!is.validVersion(version, extended = TRUE)
      && !class(try(checkLocalVersion(version), silent = T)) == "try-error")
    version <- checkLocalVersion(version, silent = F)

  # requested version not in local versions
  # look for remote version, error otherwise
  if (!(version %in% localVersions$version)) {
    if (is.offline)
      stop(paste("Version", version, "does not match any local versions",
                 "(", paste(localVersions$version, collapse = ", "), ")",
                 "and we cannot look for remote version",
                 "(either offline or amap-dev.cirad.fr unreachable).",
                 "Please go online or use a local version."),
           call. = FALSE)
    # check availability in remote versions
    remoteVersions <- getRemoteVersions()
    if (!(version %in% remoteVersions$version)) {
      # requested version not available in remote versions
      # looking for approaching remote version
      if (!class(try(checkRemoteVersion(version), silent = T)) == "try-error")
        version <- checkRemoteVersion(version, silent = F)
      else
        stop(paste("Version", version, "does not match any local versions (",
                   paste(localVersions$version, collapse = ", "),
                   "), nor any remote versions (",
                   paste(getRemoteVersions()$version, collapse = ", ")),
             call. = FALSE)
    }
  }

  # check for updates
  if (check.update) {
    # check for updates
    remoteVersions <- getRemoteVersions()
    latestVersion <- tail(remoteVersions$version, 1)
    if (compVersion(version, latestVersion) < 0) {
      message(paste("Check for updates. Latest version available", latestVersion))
      version <- latestVersion
    }
  }

  # install remote version locally if needed
  if (!(version %in% localVersions$version))
    installVersion(version)

  return(version)
}

#' @export
getRemoteVersions <- function() {

  # read AMAPVox download page
  downloadPage <- try(
    rvest::read_html("https://amap-dev.cirad.fr/projects/amapvox/files"))
  # handle read_html failure
  if (class(downloadPage)[1L] == "try-error") {
    stop(paste("AMAPVox download page is unreachable",
            "(https://amap-dev.cirad.fr/projects/amapvox/files).",
            "Please check your internet connexion or try again later."),
         call. = FALSE)
  }

  # extract all download files from HTML document
  files <- downloadPage %>%
    rvest::html_element("table") %>%
    rvest::html_nodes(".filename") %>%
    rvest::html_element("a") %>%
    rvest::html_attr("href")
  # extract AMAPVox zip files only
  url <- paste0("https://amap-dev.cirad.fr",
                grep("*AMAPVox-\\d+\\.\\d+\\.\\d+\\.zip$", files, value = T))
  # extract AMAPVox versions and sort them
  version <- stringr::str_extract(url, "\\d+\\.\\d+\\.\\d+")
  # create dataframe and sort it along version
  zips <- data.frame(version, url)
  zips <- zips[orderVersions(zips$version),]
  rownames(zips) <- NULL
  # return dataframe
  return(zips)
}

#' @export
getLocalVersions <- function() {

  # local directory for AMAPVox binaries
  binPath <- file.path(rappdirs::user_data_dir("AMAPVox"), "bin")
  # local directory does not exist, no local version yet
  if (!dir.exists(binPath))
    return(NULL)
  # list existing folders
  version <- list.dirs(binPath, full.names = FALSE, recursive = FALSE)
  version <- stringr::str_extract(version, "\\d+\\.\\d+\\.\\d+")
  path <- list.dirs(binPath, full.names = TRUE, recursive = FALSE)
  # create dataframe and sort it along version
  binaries <- data.frame(version, path)
  binaries <- binaries[orderVersions(binaries$version),]
  rownames(binaries) <- NULL
  # return dataframe
  return(binaries)
}

orderVersions <- function(versions) {

  # valid version numbers only
  stopifnot(all(sapply(versions, is.validVersion)))
  #
  split <- t(sapply(strsplit(versions, "\\."), as.integer))
  vrs <- data.frame(major = split[, 1], minor = split[, 2], build = split[, 3])
  return(order(vrs$major, vrs$minor, vrs$build))
}

compVersion <- function(v1, v2) {

  # valid version numbers only
  stopifnot(all(
    is.validVersion(v1, extended = TRUE),
    is.validVersion(v2, extended = TRUE)))
  # reformat as R package version number
  v1 <- gsub("\\.(\\d+)$", "-\\1", v1)
  v2 <- gsub("\\.(\\d+)$", "-\\1", v2)
  # compare with utils::compareVersion
  return(compareVersion(v1, v2))
}

is.validVersion <- function(version, extended = FALSE) {
  # regex pattern for version number major.minor(.build)
  pattern <- "^(\\d+)(\\.\\d+)?(\\.\\d+)$"
  if (!grepl(pattern, version)) {
    message(paste(version,
                  "is not a valid version number.",
                  "Must be \"l.m(.n)\" with l, m & n integers"))
    return(FALSE)
  }
  return(ifelse(extended,
                length(strsplit(version, "\\.")[[1L]]) == 3,
                TRUE))
}

checkVersion <- function(version, versions, silent = TRUE) {

  stopifnot(is.validVersion(version))
  # version matches remote version, check successful
  if (version %in% versions) return(version)
  # version does not match, try with short version major.minor without build
  shortVersions <- sub("\\.\\d+$", "", versions)
  shortVersion <- stringr::str_extract(version, "^\\d+\\.\\d+")
  # short version matches remote short version
  if (shortVersion %in% shortVersions) {
    # return latest build corresponding to short version
    ind <- tail(which(shortVersions == shortVersion), 1)
    suggestedVersion <- versions[ind]
    if (!silent)
      message(paste0("Requested version ", version,
                     ". Matching version ", suggestedVersion))
    return(suggestedVersion)
  }
  # short version does not match any available version
  stop(paste("Version", version,
             "does not match any available versions (",
             paste(versions, collapse = ", "), ")"))
}

#' @export
checkRemoteVersion <- function(version, silent = TRUE) {
  versions <- getRemoteVersions()
  checkVersion(version, versions$version, silent)
}

#' @export
checkLocalVersion <- function(version, silent = TRUE) {
  versions <- getLocalVersions()
  checkVersion(version, versions$version, silent)
}

#' @export
installVersion <- function(version, overwrite = FALSE) {

  # make sure version number is valid and available
  stopifnot(is.validVersion(version))
  remoteVersions <- getRemoteVersions()
  stopifnot(version %in% remoteVersions$version)

  # local directory for AMAPVox binaries
  binPath <- file.path(rappdirs::user_data_dir("AMAPVox"), "bin")
  versionPath <- file.path(binPath, paste0("AMAPVox-", version))
  # check whether requested version already installed
  jarPath <- file.path(versionPath, paste0("AMAPVox-", version, ".jar"))
  if (file.exists(jarPath) & !overwrite) {
    message(paste(version, "already installed in", versionPath))
    return(versionPath)
  }
  # url to download
  url <- remoteVersions$url[which(remoteVersions == version)]
  # local destination
  zipfile <- file.path(binPath, paste0(version, ".zip"))
  # create local bin folder if does not exist
  if (!dir.exists(binPath)) dir.create(binPath, recursive = T, showWarnings = F)
  # download zip
  download.file(url, zipfile)
  # unzip
  unzip(zipfile, exdir = versionPath)
  # delete zip file
  file.remove(zipfile)
  message(paste(version, "successfully installed in", versionPath))
  return(versionPath)
}

#' @export
removeVersion <- function(version) {

  # make sure version number is valid
  stopifnot(is.validVersion(version, extended = TRUE))

  localVersions <- getLocalVersions()
  # version not installed
  if (!(version %in% localVersions$version)) {
    warning(paste("Version", version, "not installed locallly. Nothing to do."),
            call. = FALSE, immediate. = TRUE)
    return(invisible(NULL))
  }
  # local version exists, uninstall it
  path <- localVersions$path[which(localVersions == version)]
  ifelse(
    unlink(path, recursive = TRUE) == 0,
    message(paste("Version", version, "successfully removed from computer",
                  "(", path, ").")),
    message(paste("Failed to delete folder", path,
                  ", you may have to do so manually..."))
  )
}
