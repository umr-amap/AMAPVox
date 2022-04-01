
#' @rdname clear
setMethod("clear", signature(vxsp="VoxelSpace", vx="data.table"),
          function(vxsp, vx) {

            # ensure existence of i, j, k
            stopifnot(all(c("i", "j", "k") %in% colnames(vx)))

            # clear voxels
            var.cleared <- c("nbEchos", "bsIntercepted", "PadBVTotal",
                             "attenuation_FPL_biasedMLE",
                             "attenuation_FPL_biasCorrection",
                             "attenuation_PPL_MLE")
            for (var in var.cleared) {
              if (var %in% names(vxsp)) {
                vxsp@data[vx, (var):=0]
              }
            }
            # special case for transmittance, set to 1
            if ("transmittance" %in% names(vxsp)) {
              vxsp@data[vx, ("transmittance"):= 1]
            }

          })

#' @rdname clear
setMethod("clear", signature(vxsp="VoxelSpace", vx="vector"),
          function(vxsp, vx) {

            # 3 coordinates i, j, k
            stopifnot(length(vx) == 3)
            # i, j, k must be positive integers
            stopifnot(as.integer(vx) == vx)
            stopifnot(all(vx >=0))
            # check i, j, k ranges
            stopifnot(all((vx >= 0) & (vx < dim(vxsp))))

            return (
              callGeneric(vxsp,
                          data.table::data.table(i=vx[1], j=vx[2], k=vx[3])))
          })

#' @rdname clear
setMethod("clear", signature(vxsp="VoxelSpace", vx="matrix"),
          function(vxsp, vx) {

            # 3 columns i, j, k
            stopifnot(ncol(vx) == 3)
            # i, j, k must be integers
            stopifnot(as.integer(vx) == vx)
            # check i, j, k ranges
            stopifnot(
              all(apply(vx, 1, function(vx) (vx >= 0) & (vx < dim(vxsp)))))

            return (
              callGeneric(vxsp,
                          data.table::data.table(i=vx[,1], j=vx[,2], k=vx[,3])))
          })

#' Voxel layer to raster
#'
#' @docType methods
#' @rdname toRaster
#'
#' @description Converts a voxel space (i, j) layer into a [terra::SpatRaster-class] object.
#'
#' @param vxsp a \code{\link{VoxelSpace-class}} object.
#' @param vx a voxel space horizontal slice. A data.table with `i, j` columns and
#' least one additional variable, the value of the raster layer. Every column beside i and j will be converted into a raster layer.
#'
#' @return A [terra::SpatRaster-class] object.
#'
#' @examples
#' \dontrun{
#' vxsp <- readVoxelSpace(system.file("extdata", "tls_sample.vox", package = "AMAPVox"))
#' library(terra)
#'
#' # CHM, DEM and PAI as raster
#' plot(toRaster(vxsp, merge(canopyHeight(vxsp), groundElevation(vxsp), all = T)))
#'
#' # PAI
#' vxsp <- plantAreaDensity(vxsp)
#' pai <- plantAreaIndex(vxsp, type = "xy", pattern.pad = "pad_transmittance")
#' plot(toRaster(vxsp, pai))
#'
#' # sampling intensity at 2 meters
#' plot(toRaster(vxsp, vxsp@data[ground_distance == 2.25, .(i, j, nbSampling)]))
#' }
#'
#' @export
toRaster <- function(vxsp, vx) {

  # check for terra package
  if (!requireNamespace("terra", quietly = TRUE)) {
    stop(
      "Package \"terra\" must be installed to convert voxel layer into raster.",
      "\n",
      "> install.packages(\"terra\")",
      call. = FALSE)
  }

  # must be a voxel space
  stopifnot(is.VoxelSpace(vxsp))

  # vx must be data.table with i, j columns
  stopifnot(any(class(vx) == "data.table"))
  stopifnot(c("i", "j") %in% names(vx))

  # there must be a third column beside i, j
  stopifnot(ncol(vx) >= 3)

  # i, j coordinates must be unique (no vertical dimension)
  i <- j <- N <- .N <- NULL # trick to get rid of R CMD check warning with data.table
  if (vx[, .N, by=list(i, j)][, all(N > 1)])
    stop("The `vx` layer must have unique (i, j) coordinates.")

  # layers name
  layers.name <- colnames(vx)[! colnames(vx) %in% c("i", "j")]

  nx <- dim(vxsp)["x"]
  ny <- dim(vxsp)["y"]
  xmin <- AMAPVox::getMinCorner(vxsp)["x"]
  ymin <- AMAPVox::getMinCorner(vxsp)["y"]
  xmax <- AMAPVox::getMaxCorner(vxsp)["x"]
  ymax <- AMAPVox::getMaxCorner(vxsp)["y"]

  # terra::raster and AMAPVox voxel space does not have same convention for
  # plot origin, so reorder cell index
  ind <- 1 + vx[["i"]] + nx * (ny - vx[["j"]] - 1)

  # raster layers list
  r <- list()

  # loop on layers
  for (layer.name in layers.name) {
    layer <- rep(NA, length.out <- nx * ny)
    layer[ind] <- vx[[layer.name]]
    r[[layer.name]] <- terra::rast(nrows = ny, ncols = nx,
                xmin = xmin, xmax = xmax, ymin = ymin, ymax = ymax,
                vals = layer)
  }

  # stack layers into a single raster
  return ( terra::rast(r))
}
