#' @title VoxelSpace
#' @description Class that holds the state variables of every voxel of the voxel
#'   space in a \code{\link{data.table}}, plus metadata from the voxel space
#'   header.
#' @docType class
#' @field file the path of the voxel file (.vox).
#' @field voxels the voxels hold in a data.table.
#' @field parameters a list of parameters associated to this voxel file.
#' @return An object of class VoxelSpace.
#' @seealso \code{\link{readVoxelSpace}}
#' @name VoxelSpace-class
#' @rdname VoxelSpace-class
#' @export
setClass(

  Class="VoxelSpace",
  contains="data.table",
  slots=c(
    file="character",
    voxels="data.table",
    parameters="list"
  )
)
