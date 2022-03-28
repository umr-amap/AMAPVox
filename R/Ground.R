#' Extract ground layer from voxel space.
#'
#' @docType methods
#' @rdname ground
#'
#' @description Extract ground layer from \code{\link{VoxelSpace-class}} object.
#' The ground layer is the set of voxels that are just above ground level. The
#' bottom facet of the voxel must be above ground. Ground layer may be missing
#' or incomplete for some voxel. A warning is thrown in such case.
#'
#' @return either a [data.table::data.table-class] object with voxel index of
#' the ground layer or a [terra::rast] raster with 3 layers: k-index,
#' z-elevation and ground_distance.
#'
#' @param vxsp a \code{\link{VoxelSpace-class}} object.
#' @param as.raster a boolean, whether to return the ground layer as `raster` or
#' a `data.table`.
#'
#' @export
groundLayer <- function(vxsp, as.raster = FALSE) {

  # must be a voxel space
  stopifnot(is.VoxelSpace(vxsp))

  # terra package required for returning ground layer as raster
  if (as.raster & !requireNamespace("terra", quietly = TRUE)) {
    stop(
      "Package \"terra\" must be installed to return ground layer as a raster",
      "\n",
      "> install.packages(\"terra\")",
      call. = FALSE)
  }

  # pointer to voxels data.table
  vx <- vxsp@data

  # ground_distance variable required
  stopifnot("ground_distance" %in% colnames(vx))

  # z voxel size
  dz <- getVoxelSize(vxsp)["z"]

  # extract ground layer
  i <- j <- k <- ground_distance <- NULL # trick to avoid "no visible binding" note
  groundDT <- vx[
    , list(k, ground_distance), by=list(i, j)][
      ground_distance >= (0.5 * dz) & ground_distance < (1.5 * dz)]

  # missing/incomplete ground layer
  if (nrow(groundDT) == 0) {
    warning("Ground layer is missing in voxel space")
    #return ( groundDT )
  } else if (nrow(groundDT) < prod(dim(vxsp)[1:2])) {
    warning("Some ground cells are missing in voxel space")
  }

  # elevation of ground layer
  groundDT[["z"]] <- getPosition(vxsp, groundDT)$z

  if (as.raster == TRUE) {
    # return ground layer as raster stack
    r <- c(toRasterLayer(vxsp, groundDT, "k"),
           toRasterLayer(vxsp, groundDT, "z"),
           toRasterLayer(vxsp, groundDT, "ground_distance"))
    names(r) <- c("k", "z", "ground_distance")
    return ( r )
  } else {
    # return ground layer as data.table
    return ( groundDT )
  }
}

# internal function to export specific voxel space (i, j) slice as a raster
# layer
toRasterLayer <- function(vxsp, dt, variable) {

  nx <- dim(vxsp)["x"]
  ny <- dim(vxsp)["y"]
  xmin <- AMAPVox::getMinCorner(vxsp)["x"]
  ymin <- AMAPVox::getMinCorner(vxsp)["y"]
  xmax <- AMAPVox::getMaxCorner(vxsp)["x"]
  ymax <- AMAPVox::getMaxCorner(vxsp)["y"]
  # terra::raster and AMAPVox voxel space does not have same convention for
  # plot origin, so reorder cell index
  ind <- 1 + dt[["i"]] + nx * (ny - dt[["j"]] - 1)
  #
  layer <- rep(NA, length.out <- nx * ny)
  layer[ind] <- dt[[variable]]
  return (
    terra::rast(nrows = ny, ncols = nx,
                xmin = xmin, xmax = xmax, ymin = ymin, ymax = ymax,
                vals = layer)
  )
}
