#' @title VoxHeader
#' @description Class containing metadata that are read in the header of the VoxelSpace file.
#' @docType class
#' @field mincorner the (x, y, z) coordinates of the bottom left corner of the voxel space
#' @field maxcorner the (x, y, z) coordinates of the top right corner of the voxel space
#' @field split (nx, ny, nz) the number of voxels along x, y & z axis
#' @field resolution (x, y, z) the elemental voxel size
#' @field columnNames the names of the output variables in the voxel space. Column names of the data.table in the \code{\link{VoxelSpace-class}} object.
#' @return An object of class VoxHeader
#' @name VoxHeader-class
#' @rdname VoxHeader-class
setClass(

  Class="VoxHeader",
  slots=c(
    mincorner="numeric",
    maxcorner="numeric",
    split="numeric",
    resolution="numeric",
    columnNames="vector",
    nline="integer",
    parameters="character"
  )
)

#' @title VoxelSpace
#' @description Class that holds the state variables of every voxel of the voxel space in a \code{\link{data.table}}, plus metadata from the voxel space header.
#' @docType class
#' @field file the path of the voxel file (.vox).
#' @field header the \code{\link{VoxHeader-class}} object associated to this voxel file.
#' @field voxels the voxels hold in a data.table.
#' @return An object of class VoxelSpace.
#' @seealso \code{\link{readVoxelSpace}}
#' @name VoxelSpace-class
#' @rdname VoxelSpace-class
#' @export
setClass(

  Class="VoxelSpace",
  slots=c(
    file="character",
    header="VoxHeader",
    voxels="list"
  )
)
