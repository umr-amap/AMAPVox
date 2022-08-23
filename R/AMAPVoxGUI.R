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
#'   either [Java 1.8 64-Bit Oracle](https://java.com/download/)
#'   or [Java 1.8 64-Bit Corretto](https://aws.amazon.com/fr/corretto/).
#'   OpenJDK 8 will not work since JavaFX is not included.
#'   You may check beforehand if java is installed on your system and
#'   which version.
#'   ```
#'   system2("java", args = "-version")
#'   ```
#'   If AMAPVox::gui keeps throwing errors after you have installed suitable
#'   Java 1.8 64-Bit, it means that Java 1.8 may not be properly detected by
#'   your system. In such case you may have to check and set the
#'   `JAVA_HOME` environment variable.
#'   ```
#'   Sys.getenv("JAVA_HOME")
#'   Sys.setenv(JAVA_HOME="path/to/java/1.8/bin")
#'   system2("java", args = "-version")
#'   ```
#'   As a last resort you may change the `java` parameter of this function
#'   and set the full path to Java 1.8 binary.
#'   ```
#'   AMAPVox::gui(java = "/path/to/java/1.8/bin/java")
#'   ```
#' @param version, either "latest" or a valid version number major.minor(.build)
#'   if `version="latest"` the function looks for latest remote version. If
#'   there is no internet connection it runs latest local version.
#' @param java Path to the java executable. Default 'java' value assumes that
#'   java is correctly defined on the $PATH variable.
#' @param jvm.options JVM (Java Virtual Machine) options. By default it
#'   allocates 2Go of heap memory to AMAPVox.
#' @param stdout where output to both stdout/stderr should be sent. Same as
#' stdout & stderr options from function [system2()].
#' @seealso [getLocalVersions()], [getRemoteVersions()], [installVersion()] and
#'  [removeVersion()]
#' @examples
#' \dontrun{
#' # install and run latest AMAPVox version
#' AMAPVox::gui()
#' # install and run version 1.6.4 for instance
#' AMAPVox::gui(version="1.6.4", check.update = FALSE)
#' }
#' @include VersionManager.R
#' @export
gui <- function(version="latest",
                java = "java", jvm.options = "-Xms2048m",
                stdout = "") {

  # handle versions
  version <- versionManager(version)

  # look for java and check version
  res <- suppressWarnings(
    system2(java, args = "-version", stdout = NULL, stderr = NULL))
  if (res != 0) {
    stop(paste("R did not find 'java' command.",
               " Make sure Java 1.8 64-Bit is properly installed"))
  } else {
    # java is installed, make sure it is Java 8 64-Bit
    jvrs <- system2(java, args = "-version", stdout = TRUE, stderr = TRUE)
    # java 1.8.0 64-Bit Oracle or Corretto for JavaFX support
    if (!(grepl("1\\.8\\.0", jvrs[1])
          & (grepl("Java\\(TM\\)", jvrs[2]) | grepl("Corretto", jvrs[2]))
          & grepl("64\\-[bB]it", jvrs[3]))) {
      stop("unsupported java version\n", paste("  ", jvrs, "\n"),
           "Must be Java 1.8 64-Bit, Oracle or Corretto.\n",
           "Read help function for details.")
    }
  }

  # no JVM options
  if(is.null(jvm.options)) jvm.options = ""

  localVersions <- getLocalVersions()
  amapvox <- localVersions[which(localVersions == version), ]

  # Generate the execution expression
  jarPath <- normalizePath(
    file.path(amapvox$path, paste0(
      ifelse(compVersion(version, "1.10.1") >= 0, "AMAPVoxGUI-", "AMAPVox-"),
      version, ".jar")),
    mustWork = TRUE)
  args = paste(jvm.options, "-jar", jarPath)
  command = paste(c(shQuote(java), args), collapse = " ")

  # run AMAPVox
  message(paste("Running AMAPVox", version))
  message(command)
  system2(java, args = args, stdout = stdout, stderr = stdout,
          wait = TRUE, invisible = FALSE)
  message("Closing AMAPVox.")

  return(invisible(command))
}
