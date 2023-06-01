#' Crop voxel space
#'
#' @docType methods
#' @rdname crop
#'
#' @description Crop [`VoxelSpace-class`] object based on voxel i, j,
#' k, index. If cropping index are missing, the function will automatically crop
#' the voxel space by discarding outermost unsampled slices of voxels. A *slice*
#' designates a layer with constant i (i-slice), j (j-slice) or k (k-slice).
#' *unsampled* means that no pulse went through.
#'
#' One may want to crop the voxel space on coordinates rather than grid index.
#' To do so the voxel space must be first converted to an [`sf::sf`]
#' object and use the [sf::st_crop()] function.
#' ```
#' vxsp <- readVoxelSpace(system.file("extdata", "tls_sample.vox", package = "AMAPVox"))
#' vxsp@data[, c("x", "y"):=getPosition(vxsp)[, .(x, y)]]
#' library(sf)
#' vx.sf <- sf::st_as_sf(vxsp@data, coords=c("x", "y"))
#' vx.sf <- sf::st_crop(vx.sf, c(xmin = 4, ymin = 1, xmax = 5, ymax = 4))
#' sf::st_bbox(vx.sf)
#' vxsp@data <- sf::st_drop_geometry(vx.sf)
#' ```
#'
#' @param vxsp a [`VoxelSpace-class`] object.
#' @param imin minimum i index of cropped area (inclusive)
#' @param imax maximum i index of cropped area (inclusive)
#' @param jmin minimum j index of cropped area (inclusive)
#' @param jmax maximum j index of cropped area (inclusive)
#' @param kmin minimum k index of cropped area (inclusive)
#' @param kmax maximum k index of cropped area (inclusive)
#'
#' @return Cropped voxel space with updated i, j, k grid coordinates and
#' updated header (min and max corner).
#'
#' @examples
#' \dontrun{
#' vxsp <- readVoxelSpace(system.file("extdata", "tls_sample.vox", package = "AMAPVox"))
#' plot(crop(vxsp, imin = 1, imax = 5))
#' # introduce unsampled areas in voxel space
#' vxsp@data[i < 3, nbSampling:= 0]
#' # automatic cropping
#' plot(crop(vxsp))
#' }
#'
#' @export
crop <- function(vxsp,
                 imin = 0, imax = Inf,
                 jmin = 0, jmax = Inf,
                 kmin = 0, kmax = Inf) {

  # must be a voxel space
  stopifnot(is.VoxelSpace(vxsp))

  i <- j <- k <- nbSampling <- NULL # due to NSE notes in R CMD check

  # get current max index
  vx.imax <- vxsp@data[, max(i)]
  vx.jmax <- vxsp@data[, max(j)]
  vx.kmax <- vxsp@data[, max(k)]

  # automatic crop
  if (imin == 0 & imax == Inf
      & jmin == 0 & jmax == Inf
      & kmin == 0 & kmax == Inf) {
    irange <- range(which(vxsp@data[, sum(nbSampling, na.rm = T), by = i][[2]] > 0)) - 1
    imin <- irange[1]
    imax <- irange[2]
    jrange <- range(which(vxsp@data[, sum(nbSampling, na.rm = T), by = j][[2]] > 0)) - 1
    jmin <- jrange[1]
    jmax <- jrange[2]
    krange <- range(which(vxsp@data[, sum(nbSampling, na.rm = T), by = k][[2]] > 0)) - 1
    kmin <- krange[1]
    kmax <- krange[2]
  }

  # replace negative min index by zero, replace Inf index by max index
  if (imin < 0) imin <- 0
  if (imax == Inf) imax <- vx.imax
  if (jmin < 0) jmin <- 0
  if (jmax == Inf) jmax <- vx.jmax
  if (kmin < 0) kmin <- 0
  if (kmax == Inf) kmax <- vx.kmax

  # crop index must be positive numeric
  stopifnot(is.numeric(imin), is.numeric(imax),
            is.numeric(jmin), is.numeric(jmax),
            is.numeric(kmin), is.numeric(kmax))
  stopifnot(all(c(imin, imax, jmin, jmax, kmin, kmax) >= 0))

  # min <= max
  stopifnot(imin <= imax, jmin <= jmax, kmin <= kmax)

  # check whether there is anything to crop
  if (imin == 0 & imax == vx.imax
      & jmin == 0 & jmax == vx.jmax
      & kmin == 0 & kmax == vx.kmax) {
    # nothing to do
    message("Nothing to crop")
    return(vxsp)
  }

  # crop
  vx.cropped <- vxsp@data[i >= imin & i <= imax
                            & j >= jmin & j <= jmax
                            & k >= kmin & k <= kmax, ]
  # update i, j, k index
  vx.cropped[, i:=(i-imin)]
  vx.cropped[, j:=(j-jmin)]
  vx.cropped[, k:=(k-kmin)]

  # update header parameters
  mincorner <- unlist(getPosition(vxsp, c(imin, jmin, kmin)))
  maxcorner <- unlist(getPosition(vxsp, c(imax, jmax, kmax)))
  dim <- c(imax - imin + 1, jmax - jmin + 1, kmax - kmin + 1)

  # cropped voxel space
  vxsp.cropped <- new(Class=("VoxelSpace"))
  vxsp.cropped@file <- vxsp@file
  vxsp.cropped@header <- vxsp@header

  vxsp.cropped@header$mincorner <- mincorner
  vxsp.cropped@header$maxcorner <- maxcorner
  vxsp.cropped@header$dim <- dim

  # overwrite voxels
  vxsp.cropped@data <- vx.cropped

  # return cropped voxel space
  return(vxsp.cropped)
}
