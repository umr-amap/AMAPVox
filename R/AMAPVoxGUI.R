#' @include AMAPVoxVersionManager.R
#' @export
gui <- function(version="latest", check.update = TRUE) {

  # handle versions
  version <- versionManager(version, check.update)

  # look for java
  res <- suppressWarnings(system2("java", args = "-version", stdout = NULL, stderr = NULL))
  if (res != 0) {
    stop(paste("R did not find 'java' command.",
               " Make sure Java 1.8 64-Bit is properly installed"))
  }

  # run AMAPVox
  message(paste("Running AMAPVox", version))
  localVersions <- getLocalVersions()
  amapvox <- localVersions[which(localVersions == version), ]
  if (.Platform$OS.type == "unix") {
    system2("sh", args = file.path(amapvox$path, "AMAPVox.sh"))
  } else if (.Platform$OS.type == "windows") {
    system2("cmd.exe", args = file.path(amapvox$path, "AMAPVox.bat"))
  } else {
    stop("Unsupported OS ", .Platform$OS.type)
  }
  message("Quit AMAPVox.")
}
