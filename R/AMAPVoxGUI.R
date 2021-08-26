#' @include AMAPVoxVersionManager.R
#' @export
gui <- function(version="latest", check.update = TRUE, java="java") {

  # handle versions
  version <- versionManager(version, check.update)

  # run AMAPVox
  message(paste("Running AMAPVox", version))
}
