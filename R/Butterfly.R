#' Identify butterflies from a VoxelSpace object.
#'
#' @docType methods
#' @rdname butterfly
#' @description Identify butterflies from a [`VoxelSpace-class`] object.
#'
#' A butterfly refers to a non-empty isolated voxel. Non-empty means that there
#' is one or more hits recorded in the voxel. Isolated means that voxels in the
#' [Moore neighborhood](https://en.wikipedia.org/wiki/Moore_neighborhood)
#' of rank 1 are empty (no hit).
#'
#' @return a list of voxel index (i, j, k) identified as butterfly.
#' @param vxsp a [`VoxelSpace-class`] object
#' @examples
#' # load a voxel file
#' vxsp <- readVoxelSpace(system.file("extdata", "tls_sample.vox", package = "AMAPVox"))
#' # identify butterflies
#' btf <- butterfly(vxsp)
#' # clear butterflies
#' clear(vxsp, butterfly(vxsp))
#' @seealso [clear()]
#' @export
butterfly <- function(vxsp) {

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

  return( vx.hit[butterflies, list(i, j, k)] )
}
