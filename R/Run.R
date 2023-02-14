#' Run AMAPVox
#'
#' @docType methods
#' @rdname run
#' @description Run AMAPVox either in batch mode or with Graphical User
#'   Interface (GUI). The function embeds a version manager for installing
#'   locally any version available remotely. AMAPVox relies on Java 64 bit. It
#'   has been compiled with JDK 8 64-Bit Oracle. For running AMAPVox in batch
#'   mode, any version of Java 64-bit >= 8 should work. AMAPVox GUI relies
#'   additionally on JavaFX. Refer to following section for installing a
#'   suitable Java/JavaFX.
#'
#'   `gui` function as been kept for background compatibility. It is an
#'   alias of the `run` function.
#' @section Java 1.8 64-Bit with JavaFX: AMAPVox GUI relies on Java 1.8 64-Bit
#'   and JavaFX. In practice it requires either [Java 1.8 64-Bit
#'   Oracle](https://java.com/download/) or [Java 1.8 64-Bit
#'   Corretto](https://aws.amazon.com/fr/corretto/). OpenJDK 8 will not work
#'   since JavaFX is not included. You may check beforehand if java is installed
#'   on your system and which version.
#'   ```
#'   system2("java", args = "-version")
#'   ```
#'   If AMAPVox::gui keeps throwing errors after you have installed suitable
#'   Java 1.8 64-Bit, it means that Java 1.8 may not be properly detected by
#'   your system. In such case you may have to check and set the `JAVA_HOME`
#'   environment variable.
#'   ```
#'   Sys.getenv("JAVA_HOME")
#'   Sys.setenv(JAVA_HOME="path/to/java/1.8/bin")
#'   system2("java", args = "-version")
#'   ```
#'   As a last resort you may change the `java` parameter of
#'   this function and set the full path to Java 1.8 binary.
#'   ```
#'   AMAPVox::run(java = "/path/to/java/1.8/bin/java")
#'   ```
#' @param version, either "latest" or a valid version number major.minor(.build)
#'   if `version="latest"` the function looks for latest remote version. If
#'   there is no internet connection it runs latest local version.
#' @param xml path(s) to AMAPVox XML configuration files. If missing or `NULL`
#'   AMAPVox launches the GUI.
#' @param java path to the java executable. Default 'java' value assumes that
#'   java is correctly defined on the $PATH variable.
#' @param jvm.options JVM (Java Virtual Machine) options. By default it
#'   allocates 2Go of heap memory to AMAPVox.
#' @param nt maximum number of threads for running tasks. `nt=1` means
#'   sequential execution. `nt=0` means as many threads as available.
#' @param ntt maximum number of threads per task. `ntt=0` means as many threads
#'   as available.
#' @param stdout where output from both stdout/stderr should be sent. Same as
#'   stdout & stderr options from function [system2()].
#' @seealso [getLocalVersions()], [getRemoteVersions()], [installVersion()] and
#'   [removeVersion()]
#' @examples
#' \dontrun{
#' # (install and )run latest AMAPVox version with GUI
#' AMAPVox::run()
#' # (install and )run version 1.6.4 with GUI
#' AMAPVox::run(version="1.6.4")
#' # run latest AMAPVox version with XML configuration
#' AMAPVox::run(xml="/path/to/cfg.xml")
#' # run multiple configurations
#' AMAPVox::run(xml=c("cfg1.xml", "cfg2.xml"), nt=2)
#' }
#' @include VersionManager.R
#' @export
run <- function(version="latest",
                xml,
                java = "java", jvm.options = "-Xms2048m",
                nt = 1, ntt = 1,
                stdout = "") {

  # handle versions
  version <- versionManager(version)

  # no JVM options
  if(is.null(jvm.options)) jvm.options = ""

  # local AMAPVox
  localVersions <- getLocalVersions()
  amapvox <- localVersions[which(localVersions == version), ]

  # look for java and check version
  if (is_v1(version))
    check.java(java)
  else
    java <- ifelse(get_os() == "windows",
                   file.path(amapvox$path, "runtime", "bin", "java.exe"),
                   file.path(amapvox$path, "lib", "runtime", "bin", "java"))

  # Generate the execution expression
  if (missing(xml) || is.null(xml)) {
    # AMAPVox GUI
    if (compVersion(version, "1.10.1") < 0) {
      jar.path <- file.path(amapvox$path,
                            paste0("AMAPVox-", version, ".jar"))
    } else if (compVersion(version, "2.0.0") < 0) {
      jar.path <- file.path(amapvox$path,
                            paste0("AMAPVoxGUI-", version, ".jar"))
    } else {
      if (get_os() == "windows") {
        jar.path <- file.path(amapvox$path,
                              "app",
                              paste0("AMAPVox-", version, ".jar"))
      } else {
        jar.path <- file.path(amapvox$path,
                              "lib", "app",
                              paste0("AMAPVox-", version, ".jar"))
      }
    }
    # normalize path
    jar.path <- normalizePath(jar.path, mustWork = TRUE)
    # JVM options
    args = ifelse(is_v1(version),
                  paste(jvm.options, "-jar", jar.path),
                  paste(jvm.options,
                        "--add-opens javafx.graphics/javafx.scene=ALL-UNNAMED",
                        "-jar", jar.path))
  } else {
    # AMAPVox batch mode
    # configuration file must exist
    stopifnot(all(file.exists(xml)))
    if (is_v1(version)) {
      jar.path <- file.path(amapvox$path,
                            paste0("AMAPVox-", version, ".jar"))
    } else {
      if (get_os() == "windows") {
        jar.path <- file.path(amapvox$path,
                              "app",
                              paste0("AMAPVox-", version, ".jar"))
      } else {
        jar.path <- file.path(amapvox$path,
                              "lib", "app",
                              paste0("AMAPVox-", version, ".jar"))
      }
    }
    jar.path <- normalizePath(jar.path, mustWork = TRUE)
    # validate number of threads and number of threads per task
    stopifnot(all(is.wholenumber(nt), nt >= 0, is.wholenumber(ntt), ntt >= 0))
    # jar options
    jar.options = ifelse(
      compVersion(version, "1.10.1") >= 0,
      paste(paste0("--T=", nt), paste0("--TT=", ntt), xml),
      paste(paste0("--execute-cfg=\"", paste(xml, collapse = " "), "\""),
            paste0("--T=", nt), paste0("--T-TLS_VOX=", ntt)))
    args = paste(jvm.options, "-jar", jar.path, jar.options)
  }
  # concatenate command
  command = paste(c(shQuote(java), args), collapse = " ")

  # run AMAPVox
  message(paste("Running AMAPVox", version))
  message(command)
  if (get_os() == "windows")
    system2(java, args = args, stdout = stdout, stderr = stdout, wait = TRUE,
            invisible = FALSE)
  else
    system2(java, args = args, stdout = stdout, stderr = stdout, wait = TRUE)

  return(invisible(command))
}

#' @export
#' @rdname run
gui <- function(version="latest",
                java = "java", jvm.options = "-Xms2048m",
                stdout = "") {
  # call run() function
  AMAPVox::run(version, xml= NULL, java, jvm.options, stdout)
}

# internal util function
is.wholenumber <- function(x, tol = .Machine$double.eps^0.5) {
  abs(x - round(x)) < tol
}

# check java version for AMAPVox v1
# must be Java 1.8 64 bit Oracle or Corretto (sic!)
check.java <- function(java = "java") {

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
}
