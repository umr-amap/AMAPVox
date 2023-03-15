## Version manager
## Important function, not exported though, because there is no reason for a
## direct call by end user. At this stage end user shall call AMAPVox::gui().
## What is does ? Given a requested version (either "latest" or major.minor or
## major.minor.build) the function either returns the requested version or the
## best match.
## If requested version is available locally, end of story. If not, check
## remotely and install if available. If not returns best match, remote if
## online, local otherwise.
## Throws an error if no approaching version can be found.
versionManager <- function(version="latest") {

  # check internet connection
  is.offline <- inherits(
    try(curl::nslookup("forge.ird.fr"), silent = TRUE),
    "try-error")

  # list local versions
  localVersions <- getLocalVersions()
  # no local version and offline
  if (is.null(localVersions) & is.offline) {
    stop(paste("There are not any local version installed.",
               "Computer is offline, cannot look for remote version. "))
  }

  # check updates
  check.update = (version == "latest")

  # no local version, set arbitrary version 0.0
  if (is.null(localVersions)) {
    version <- "0.0"
  } else if (version == "latest") {
    # latest local version
    version <- utils::tail(localVersions$version, 1)
  }
  # valid version number l.m(.n)
  stopifnot(is.validVersion(version, expanded = FALSE))

  if (is.offline) {
    ## OFFLINE
    # resolve local version
    if (!inherits(
           try(resolveLocalVersion(version, silent = TRUE), silent = TRUE),
           "try-error")) {
      localVersion <- resolveLocalVersion(version)
      cmp <- compVersion(version, localVersion)
      # requested version older than local version
      if (cmp < 0)
        warning(paste("Computer is offline, cannot check if version", version,
                      "is available online."),
                call. = FALSE, immediate. = TRUE)
      # requested version newer than local version
      if  (cmp > 0)
        warning(paste("Computer is offline, cannot check if a newer version",
                      version, "is available online."),
                call. = FALSE, immediate. = TRUE)
      # cannot check update offline
      if (check.update)
        warning("Computer is offline, cannot check for update.",
                call. = FALSE, immediate. = TRUE)
      version <- localVersion
    }
    else
      stop(paste("Version", version, "does not match any local versions",
                 "(", paste(localVersions$version, collapse = ", "), ").\n",
                 "Computer is offline, cannot check if version", version,
                 "is available online."),
           call. = FALSE)
  } else {
    ## ONLINE
    # list remote versions
    remoteVersions <- getRemoteVersions()
    latestVersion <- utils::tail(remoteVersions, 1)$version
    # update requested
    if (check.update && (compVersion(version, latestVersion) < 0)) {
      version <- latestVersion
      message(paste("Check for updates. Latest version available",
                    latestVersion))
    }
    # check local version availability
    if (!(version %in% localVersions$version)) {
      # resolve remote version
      version <- resolveRemoteVersion(version)
      # install remote version
      installVersion(version)
    }
  }

  return(version)
}

#' List remote AMAPVox versions.
#'
#' @docType methods
#' @rdname getRemoteVersions
#' @description List AMAPVox versions available for download from AMAPVox Gitlab
#'   package registry \url{https://forge.ird.fr/amap/amapvox/-/packages}
#' @return a `data.frame` with 2 variables: `$version` that stores
#'   the version number and `$url` the URL of the associated ZIP file.
#' @seealso [getLocalVersions()]
#' @export
getRemoteVersions <- function() {

  # get list of packages
  url <- "https://forge.ird.fr/api/v4/projects/421/packages?package_type=generic"
  req <- curl::curl_fetch_memory(url)
  pkgs <- jsonlite::fromJSON(jsonlite::prettify(rawToChar(req$content)))

  # keep only AMAPVox packages
  pkgs <- pkgs[pkgs$name == "amapvox", ]

  # add url to list package files
  url <- "https://forge.ird.fr/api/v4/projects/421/packages"
  pkgs <- cbind(pkgs,
                files_path=paste(url, pkgs$id, "package_files", sep="/"))

  # list package files
  zips <- data.table::rbindlist(apply(pkgs, 1, function(pkg) {
    req <- curl::curl_fetch_memory(pkg$files_path)
    pkg.files <- jsonlite::fromJSON(jsonlite::prettify(rawToChar(req$content)))
    return( data.table::data.table(version=pkg$version, file_name=pkg.files$file_name) )
  }))
  url <- "https://forge.ird.fr/api/v4/projects/421/packages/generic/amapvox"
  version <- file_name <- NULL # trick to avoid "no visible binding" note
  zips[, url:=paste(url, version, file_name, sep="/")][, file_name:=NULL]

  # sort versions
  zips <- zips[orderVersions(zips$version),]
  # return dataframe
  return(zips)
}

#' List local AMAPVox versions.
#'
#' @docType methods
#' @rdname getLocalVersions
#' @description List AMAPVox versions already installed on your computer by
#'  the package. AMAPVox versions are installed in the user-specific data
#'  directory, as specified by [rappdirs::user_data_dir()].
#' @return a `data.frame` with 2 variables: `$version` that stores
#'   the version number and `$path` the local path of the AMAPVox
#'   directory.
#' @seealso [getRemoteVersions()], [rappdirs::user_data_dir()]
#' @export
getLocalVersions <- function() {

  # local directory for AMAPVox binaries
  binPath <- normalizePath(
    file.path(rappdirs::user_data_dir("AMAPVox"), "bin"),
    mustWork = FALSE)
  # local directory does not exist, no local version yet
  if (!dir.exists(binPath))
    return(NULL)
  # list existing folders
  version <- list.dirs(binPath, full.names = FALSE, recursive = FALSE)
  if (identical(version, character(0)))
   return(NULL)
  version <- stringr::str_extract(version, "\\d+\\.\\d+\\.\\d+")
  path <- vapply(
    list.dirs(binPath, full.names = TRUE, recursive = FALSE),
    normalizePath,
    character(1),
    USE.NAMES = F)
  # create dataframe and sort it along version
  binaries <- data.frame(version, path)
  binaries <- binaries[orderVersions(binaries$version),]
  rownames(binaries) <- NULL
  # return dataframe
  return(binaries)
}

## check if valid version number major.minor(.build) with major, minor & build
## positive integers.
## expanded option controls whether the (.build) number is mandatory or not.
is.validVersion <- function(version, expanded = TRUE) {
  # regex pattern for version number major.minor(.build)
  pattern <- "^(\\d+)(\\.\\d+)?(\\.\\d+)$"
  if (!grepl(pattern, version)) {
    message(paste(version,
                  "is not a valid version number.",
                  "Must be \"l.m(.n)\" with l, m & n integers"))
    return(FALSE)
  }
  return(ifelse(expanded,
                length(strsplit(version, "\\.")[[1L]]) == 3,
                TRUE))
}

## order version numbers respectively along major, minor and build numbers.
orderVersions <- function(versions) {

  # valid version numbers only
  stopifnot(all(vapply(versions, is.validVersion, logical(1))))
  #
  split <- t(vapply(strsplit(versions, "\\."), as.integer, integer(3)))
  vrs <- data.frame(major = split[, 1], minor = split[, 2], build = split[, 3])
  return(order(vrs$major, vrs$minor, vrs$build))
}

## compare two version numbers using the base function compareVersion
## transform "major.minor.build" into "major.minor-build" for compatibility with
## R version numbers.
compVersion <- function(v1, v2) {

  # expand version number l.m.n
  v1.exp <- expandVersion(v1)
  v2.exp <- expandVersion(v2)

  # valid version numbers only
  stopifnot(all(is.validVersion(v1.exp), is.validVersion(v2.exp)))
  # reformat as R package version number
  v1.exp <- gsub("\\.(\\d+)$", "-\\1", v1.exp)
  v2.exp <- gsub("\\.(\\d+)$", "-\\1", v2.exp)
  # compare with utils::compareVersion
  return(utils::compareVersion(v1.exp, v2.exp))
}

## expand version number major.minor to major.minor.0
expandVersion <- function(version) {

  stopifnot(is.validVersion(version, expanded = FALSE))
  return(ifelse(
    length(strsplit(version, "\\.")[[1L]]) == 3,
    version,
    paste0(version, ".999")
  ))
}

## given a version number and a list of version numbers (versions), the function
## resolves the version against the vector of version numbers.
## if version is contained in versions, it returns version
## if not it returns the latest version number from versions with matching
## major and minor numbers.
## at last throws error if none matches.
resolveVersion <- function(version, versions, silent) {

  # expand version number l.m.n
  version.exp <- expandVersion(version)
  # valid version numbers only
  stopifnot(all(vapply(rbind(version.exp, versions), is.validVersion, logical(1))))
  # version matches remote version, check successful
  if (version.exp %in% versions) return(version.exp)
  # version does not match, try with short version major.minor without build
  shortVersions <- sub("\\.\\d+$", "", versions)
  shortVersion <- stringr::str_extract(version.exp, "^\\d+\\.\\d+")
  # short version matches remote short version
  if (shortVersion %in% shortVersions) {
    # return latest build corresponding to short version
    ind <- utils::tail(which(shortVersions == shortVersion), 1)
    suggestedVersion <- versions[ind]
    if (!silent)
      message(paste0("Requested version ", version,
                     ". Matching version ", suggestedVersion))
    return(suggestedVersion)
  }
  # short version does not match any available version
  stop(paste("Version", version,
             "does not match any available versions",
             "(", paste(versions, collapse = ", "), ")"))
}

## check if remote version is available, or suggest approaching version
## otherwise.
resolveRemoteVersion <- function(version, silent = FALSE) {
  versions <- getRemoteVersions()
  resolveVersion(version, versions$version, silent)
}

## check if local version is available, or suggest approaching version
## otherwise.
resolveLocalVersion <- function(version, silent = FALSE) {
  versions <- getLocalVersions()
  resolveVersion(version, versions$version, silent)
}

#' Install specific AMAPVox version on local computer.
#'
#' @docType methods
#' @rdname installVersion
#' @description Install specific AMAPVox version on your computer.
#'   AMAPVox versions are installed in the user-specific data
#'   directory, as specified by [rappdirs::user_data_dir()].
#'   You should not worry to call directly this function since
#'   local installations are automatically handled by the version manager
#'   when you launch AMAPVox GUI with [gui()] function.
#' @param version, a valid and existing AMAPVox remote version number
#'   (major.minor.build)
#' @param overwrite, whether existing local installation should be re-installed.
#' @return the path of the AMAPVox installation directory.
#' @seealso [getLocalVersions()], [getRemoteVersions()], [removeVersion()]
#' @seealso [rappdirs::user_data_dir()]
#' @examples
#' \dontrun{
#' # install latest version
#' installVersion(tail(getRemoteVersions()$version, 1))
#' }
#' @export
installVersion <- function(version, overwrite = FALSE) {

  # make sure version number is valid and available
  stopifnot(is.validVersion(version))
  remoteVersions <- getRemoteVersions()
  stopifnot(version %in% remoteVersions$version)

  # local directory for AMAPVox binaries
  binPath <- normalizePath(
    file.path(rappdirs::user_data_dir("AMAPVox"), "bin"),
    mustWork = FALSE)
  versionPath <- normalizePath(
    file.path(binPath, ifelse(is_v1(version),
                              paste0("AMAPVox-", version),
                              paste0("AMAPVox-", version, "-", get_os()))),
    mustWork = FALSE)
  # check whether requested version already installed
  jar.dir <- ifelse(is_v1(version),
                    versionPath,
                    ifelse(get_os() == "windows",
                           file.path(versionPath, "app"),
                           file.path(versionPath, "lib", "app")))
  jarPath <- normalizePath(
    file.path(jar.dir, paste0("AMAPVox-", version, ".jar")),
    mustWork = FALSE)
  if (file.exists(jarPath) & !overwrite) {
    message(paste("AMAPVox", version, "already installed in", versionPath))
    return(versionPath)
  }
  # url to download
  url <- remoteVersions$url[which(remoteVersions == version)]
  # from AMAPVox v2 OS specific binaries
  if (!is_v1(version)) {
    # specific URL depending on OS
    url <- url[which(grepl(get_os(), url))]
    # unsupported OS
    if (url == '')
      stop("Unsupported OS (", get_os(),
           ") Sorry! Email contact@amapvox.org to request specific binaries.")
  }
  # local destination
  zipfile <- normalizePath(
    file.path(binPath, basename(url)),
    mustWork = FALSE)
  # create local bin folder if does not exist
  if (!dir.exists(binPath)) dir.create(binPath, recursive = TRUE,
                                       showWarnings = FALSE)
  # download zip
  utils::download.file(url, zipfile, method = "auto", mode="wb", timeout=300)
  # unzip
  utils::unzip(zipfile,
               # for linux-like system uses system unzip that should preserve file permissions
               unzip = ifelse(get_os() == "windows", "internal", getOption("unzip")),
               exdir = ifelse(is_v1(version), versionPath, binPath))
  # delete zip file
  file.remove(zipfile)
  message(paste("AMAPVox", version, "successfully installed in", versionPath))
  return(versionPath)
}

#' Remove specific AMAPVox version from local computer.
#'
#' @docType methods
#' @rdname removeVersion
#' @description Uninstall specific AMAPVox version from your computer.
#' @param version, a valid and existing AMAPVox local version number
#'   (major.minor.build)
#' @seealso [getLocalVersions()], [installVersion()]
#' @examples
#' \dontrun{
#' # uninstall oldest version from your computer
#' removeVersion(head(getLocalVersions()$version, 1))
#' }
#' @export
removeVersion <- function(version) {

  # make sure version number is valid
  stopifnot(is.validVersion(version))

  localVersions <- getLocalVersions()
  # version not installed
  if (!(version %in% localVersions$version)) {
    message(paste("Version", version, "not installed locallly. Nothing to do."),
            call. = FALSE, immediate. = TRUE)
    return(invisible(NULL))
  }
  # local version exists, uninstall it
  path <- localVersions$path[which(localVersions == version)]
  if (unlink(path, recursive = TRUE) == 0)
  {
    message(paste("Version", version, "successfully removed",
                  "(", path, ")."))
  } else {
    message(paste("Failed to delete folder", path,
                  ", you may have to do so manually..."))
  }
}

# get operating system 'windows', 'linux', 'osx'
get_os <- function() {
  sysinf <- Sys.info()
  if (!is.null(sysinf)){
    os <- sysinf['sysname']
    if (os == 'Darwin')
      os <- "osx"
  } else { ## mystery machine
    os <- .Platform$OS.type
    if (grepl("^darwin", R.version$os))
      os <- "osx"
    if (grepl("linux-gnu", R.version$os))
      os <- "linux"
  }
  tolower(os)
}

# check whether given version is v1
# does not even check whether it is a valid version, assuming it has been
# tested already
is_v1 <- function(version) {
  return(compVersion(version, "2.0.0") < 0)
}
