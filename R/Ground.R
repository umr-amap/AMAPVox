#' Extract ground from voxel space.
#'
#' @docType methods
#' @rdname ground
#'
#' @description Extract ground layer from \code{\link{VoxelSpace-class}} object.
#'
#' ## Ground layer
#'
#' The ground layer is the set of voxels that are just above ground level. The
#' bottom facet of the voxel must be above ground
#' `ground_distance(voxel_center) >= dz/2` with dz the voxel size on z axis.
#' Ground layer may be missing (the function returns an empty data.table) or
#' incomplete (the function returns a data.table with
#' `nrow(ground(vxsp)) < prod(dim(vxsp)[1:2])`) for some voxel space.
#'
#' ## Above/below ground
#'
#' Function `aboveGround` returns voxel index above ground layer (included).
#' Function `belowGround` returns voxel index below ground layer (excluded).
#'
#' ## Ground energy
#'
#' Function `groundEnergy` estimates fraction of light reaching the ground. It
#' is computed as the ratio of entering beam section on potential beam section
#' (beams that would have crossed a voxel if there were no vegetation in the
#' scene). It requires variables *bsEntering* and *bsPotential*.
#'
#' ## Ground elevation
#'
#' Function `groundElevation` returns the elevation of the ground layer. It is
#' provided as a check function, to make sure that AMAPVox
#' *digital elevation model* is consistent with the one provided in input.
#'
#' @return [data.table::data.table-class] object with voxel index either
#' below ground, ground level or above ground.
#'
#' @param vxsp a \code{\link{VoxelSpace-class}} object.
#'
#' @examples
#' vxsp <- readVoxelSpace(system.file("extdata", "tls_sample.vox", package = "AMAPVox"))
#' gr <- ground(vxsp)
#' ag <- aboveGround(vxsp)
#' bg <- belowGround(vxsp) # empty in test case
#' # ground layer included in above ground subset
#' all(ag[gr, on=list(i, j, k)] == gr) # TRUE expected
#  # extract above ground voxels
#' vxsp@data[ag, on=list(i, j, k)]
#'
#' @export
ground <- function(vxsp) {

  # must be a voxel space
  stopifnot(is.VoxelSpace(vxsp))

  # ground_distance variable required
  stopifnot("ground_distance" %in% names(vxsp))

  # z voxel size
  dz <- getVoxelSize(vxsp)["z"]

  # extract ground layer
  i<- j <- k <- ground_distance <- NULL # trick to avoid "no visible binding" note
  return ( vxsp@data[ground_distance >= (0.5 * dz)
                     & ground_distance < (1.5 * dz), list(i, j, k)] )
}

#' @rdname ground
#' @export
belowGround <- function(vxsp) {

  # must be a voxel space
  stopifnot(is.VoxelSpace(vxsp))

  # ground_distance variable required
  stopifnot("ground_distance" %in% names(vxsp))

  # z voxel size
  dz <- getVoxelSize(vxsp)["z"]

  # return i, j, k index of below ground voxels
  i<- j <- k <- ground_distance <- NULL # trick to avoid "no visible binding" note
  return ( vxsp@data[ground_distance < (0.5 * dz), list(i, j, k)] )
}

#' @rdname ground
#' @export
aboveGround  <- function(vxsp) {

  # must be a voxel space
  stopifnot(is.VoxelSpace(vxsp))

  # ground_distance variable required
  stopifnot("ground_distance" %in% names(vxsp))

  # z voxel size
  dz <- getVoxelSize(vxsp)["z"]

  # return i, j, k index of below ground voxels
  i<- j <- k <- ground_distance <- NULL # trick to avoid "no visible binding" note
  return ( vxsp@data[ground_distance >= (0.5 * dz), list(i, j, k)] )
}

#' @rdname ground
#' @export
groundEnergy <- function(vxsp) {

  # get ground
  ground <- ground(vxsp)

  # bsEntering and bsPotential variables required
  stopifnot(c("bsEntering", "bsPotential") %in% names(vxsp))

  # ground energy
  i <- j <- k <- bsEntering <- bsPotential <- NULL # trick to avoid "no visible binding" note
  vxsp@data[ground,
            list(i, j, ground_energy=bsEntering / bsPotential),
            on=list(i, j, k)]
}

#' @rdname ground
#' @export
groundElevation <- function(vxsp) {

  # get ground
  ground <- ground(vxsp)

  # ground elevation
  i <- j <- k <- NULL # trick to avoid "no visible binding" note
  dem <- vxsp@data[ground, list(i, j), on = list(i, j, k)]
  dem[["ground_elevation"]] <- getPosition(vxsp,
                                           vxsp@data[ground, on=list(i, j, k)])[["z"]]

  return (dem)
}
