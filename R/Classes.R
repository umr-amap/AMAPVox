#' @title VoxelSpace
#' @description Class that holds the state variables of every voxel of the voxel
#'   space in a \code{\link{data.table}}, plus metadata from the voxel space
#'   header.
#' @docType class
#' @slot file the path of the voxel file (.vox).
#' @slot data the voxels hold in a data.table.
#' @slot header a list of parameters associated to this voxel file.
#' @return An object of class VoxelSpace.
#' @seealso \code{\link{readVoxelSpace}}
#' @name VoxelSpace-class
#' @rdname VoxelSpace-class
#' @export
setClass(

  Class="VoxelSpace",
  slots=c(
    file="character",
    data="data.table",
    header="list"
  )
)
