#' Run AMAPVox Graphical User Interface.
#'
#' @docType methods
#' @rdname gui
#' @description Run AMAPVox Graphical User Interface (GUI). The function embeds
#'   a version manager for installing locally any version available remotely.
#'   AMAPVox GUI relies on Java 1.8 64-Bit and JavaFX.
#'   See detailed section below.
#' @section Java 1.8 64-Bit with JavaFX:
#'   AMAPVox GUI relies on Java 1.8 64-Bit and JavaFX. In practice it requires
#'   either \href{https://java.com/download/}{Java 1.8 64-Bit Oracle}
#'   or \href{https://aws.amazon.com/fr/corretto/}{Java 1.8 64-Bit Corretto}.
#'   OpenJDK 8 will not work since JavaFX is not included.
#'   You may check beforehand if java is installed on your system and
#'   which version.
#'   \preformatted{
#'   system2("java", args = "-version")
#'   }
#'   If AMAPVox::gui keeps throwing errors after you have installed suitable
#'   Java 1.8 64-Bit, it means that Java 1.8 may not be properly detected by
#'   your system. In such case you may have to check and set the
#'   \code{JAVA_HOME} environment variable.
#'   \preformatted{
#'   Sys.getenv("JAVA_HOME")
#'   Sys.setenv(JAVA_HOME="path/to/java/1.8/bin")
#'   system2("java", args = "-version")
#'   }
#' @param version, either "latest" or a valid version number major.minor(.build)
#'   if \code{version="latest"} and \code{check.update=FALSE} or no internet
#'   connection it runs latest local version.
#' @param check.update, check for newer version online and install it.
#' @seealso \code{\link{getLocalVersions}}, \code{\link{getRemoteVersions}},
#'   \code{\link{installVersion}}, \code{\link{removeVersion}}
#' @examples
#' \dontrun{
#' # install and run latest AMAPVox version
#' AMAPVox::gui()
#' # install and run version 1.6.4 for instance
#' AMAPVox::gui(version="1.6.4", check.update = FALSE)
#' }
#' @include VersionManager.R
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
    jvrs <- system2("java", args = "-version", stdout = TRUE, stderr = TRUE)
    # java 1.8.0 64-Bit Oracle or Corretto for JavaFX support
    if (!(grepl("1\\.8\\.0", jvrs[1])
          & (grepl("Java\\(TM\\)", jvrs[2]) | grepl("Corretto", jvrs[2]))
          & grepl("64\\-[bB]it", jvrs[3]))) {
      stop("unsupported java version\n", paste("  ", jvrs, "\n"),
           "Must be Java 1.8 64-Bit, Oracle or Corretto.\n",
           "Read help function for details.")
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
