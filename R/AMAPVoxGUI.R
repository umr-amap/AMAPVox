#' @include AMAPVoxVersionManager.R
#' @export
gui <- function(version="latest", check.update = TRUE) {

  # handle versions
  version <- versionManager(version, check.update)

  # look for java and check version
  res <- suppressWarnings(
    system2("java", args = "-version", stdout = NULL, stderr = NULL))
  if (res != 0) {
    stop(paste("R did not find 'java' command.",
               " Make sure Java 1.8 64-Bit is properly installed"))
  } else {
    # java is installed, make sure it is Java 8 64-Bit
    jversion <- system2("java", args = "-version", stdout = TRUE, stderr = TRUE)
    # java 1.8.0 64-Bit Oracle or Corretto for JavaFX support
    if (!(grepl("1\\.8\\.0", jversion[1])
          & (grepl("Java\\(TM\\)", jversion[2]) | grepl("Corretto", jversion[2]))
          & grepl("64\\-[bB]it", jversion[3]))) {
      stop("unsupported java version\n", paste("  ", jversion, "\n"),
           "Must be Java 1.8 64-Bit, Oracle or Corretto. Read help function for details.")
    }
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
