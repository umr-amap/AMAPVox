#' Remove butterflies from a VoxelSpace object.
#'
#' @docType methods
#' @rdname removeButterfly
#' @description Remove butterflies from a \code{\link{VoxelSpace-class}} object.
#'
#' A butterfly refers to a non-empty isolated voxel. Non-empty means that there
#' is one or more hits recorded in the voxel. Isolated means that voxels in the
#' \href{https://en.wikipedia.org/wiki/Moore_neighborhood}{Moore neighborhood}
#' of rank 1 are empty (no hit).
#'
#' Voxels identified as butterfly are emptied by updating some state variables,
#' namely:
#' \itemize{
#' \item{number of echo set to zero}
#' \item{intercepted beam surface set to zero (if variable is outputed)}
#' \item{plant area density set to zero (if variable is outputed)}
#' \item{transmittance set to one (if variable is outputed)}
#' \item{any attenuation variable set to zero}
#' }
#' @return a cleaned-up voxel space
#' @param vxsp a VoxelSpace object
#' @examples
#' # load a voxel file
#' vxsp <- readVoxelSpace(system.file("extdata", "tls_sample.vox", package = "AMAPVox"))
#' # remove butterflies
#' vxsp <- removeButterfly(vxsp)
#' @export
removeButterfly <- function(vxsp) {

  i <- j <- k <- nbSampling <- nbEchos <- NULL # due to NSE notes in R CMD check

  if (!requireNamespace("RANN", quietly = TRUE)) {
    stop(
      "Package \"RANN\" must be installed to remove butterfly",
      "\n",
      "> install.packages(\"RANN\")",
      call. = FALSE)
  }

  # must be a voxel space
  stopifnot(is.VoxelSpace(vxsp))

#  cat("Looking for butterflies in", basename(vxsp@file), "...", "\n")

  # pointer to voxels
  vx <- vxsp@data

  # subset of voxels with nbEchos > 0
  vx.hit.index <- vx[nbEchos > 0, which = TRUE]
  vx.hit <- vx[vx.hit.index, list(i, j, k, nbEchos)]

  # moore neighborhood of rank 1
  neighbors <- RANN::nn2(data = vx.hit[, list(i, j, k)],
                         k = 27, searchtype = "radius", radius = 1.8)
  # remove current voxel from neighbors
  neighbors <- neighbors$nn.idx[, -1]

  # identify butterflies
  # butterflies = voxels without neighbors in subset of voxels with nbEchos > 0
  butterflies <- which(
    apply(neighbors, 1, function(neighbor) all(neighbor == 0)))

  if (length(butterflies) == 0) {
    cat("No butterfly found.", "\n")
  } else {
    # clean butterflies
    vx[vx.hit[butterflies], `:=`(nbEchos = 0,
                                 bsIntercpted = 0,
                                 PadBVTotal = 0,
                                 transmittance = 1,
                                 attenuation_FPL_biasedMLE = 0,
                                 attenuation_FPL_biasCorrection = 0,
                                 attenuation_FPL_unbiasedMLE = 0,
                                 attenuation_PPL_MLE = 0),
       on = list(i, j, k)]

    cat("Removed", length(butterflies), "butterflies.", "\n")

    # only keep original columns
    vxsp@data <- vx[, .SD, .SDcols = getParameter(vxsp, "columnNames")]
  }

  return ( vxsp )
}
