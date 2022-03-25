
#' Show \code{\link{VoxelSpace-class}} object.
#'
#' @docType methods
#' @rdname show
#' @description Display the \code{\link{VoxelSpace-class}} object main
#'   characteristics.
#' @return show returns an invisible NULL.
#' @param object a \code{\link{VoxelSpace-class}} object.
#' @include Classes.R
#' @examples
#' # load a voxel file
#' vxsp <- readVoxelSpace(
#'            system.file("extdata", "tls_sample.vox", package = "AMAPVox"))
#' # show VoxelSpace object
#' show(vxsp)
#' @export
setMethod ("show",
           signature(object = "VoxelSpace"),
           function(object) showVoxelSpace(object))

##
#' @export
print.VoxelSpace <- function(x, ...) showVoxelSpace(x, ...)

#' Length of a VoxelSpace
#'
#' @docType methods
#' @rdname dim
#' @description Get the number of voxels in the voxel space.
#' @section Warning:
#' AMAPVox allows to discard empty voxels in the voxel file. In such case
#'     \code{length.VoxelSpace} will return the expected number of voxels as if
#'     none were missing. As a  consequence the number of voxels stored in the
#'     \code{\link{VoxelSpace-class}} object may be inferior to the returned
#'     value, namely \code{nrow(x@data) <= length(x)}
#' @return the number of voxels in the voxel space.
#' @param x a \code{\link{VoxelSpace-class}} object.
#' @include Classes.R
#' @export
length.VoxelSpace <- function(x) return (prod(x@header$dim))

#' Dimensions of the VoxelSpace
#'
#' @docType methods
#' @rdname dim
#' @description Retrieve the dimension of a \code{\link{VoxelSpace-class}}
#' @return the number of voxels nx, ny, nz of a \code{\link{VoxelSpace-class}}
#'   along x, y, z axis.
#' @param x a \code{\link{VoxelSpace-class}} object.
#' @include Classes.R
#' @export
dim.VoxelSpace <- function(x) return (x@header$dim)

#' Tests for objects of class VoxelSpace
#'
#' @docType methods
#' @rdname is.VoxelSpace
#' @description Tests for objects of class VoxelSpace
#' @param x to be tested
#' @include Classes.R
#' @seealso \code{\link{VoxelSpace-class}};
#' @export
is.VoxelSpace <- function (x) is(x, "VoxelSpace")

#' Gets a parameter from the VoxelSpace header.
#'
#' @docType methods
#' @rdname getParameter
#' @description Gets a parameter from the VoxelSpace header.
#' @param vxsp the \code{\link{VoxelSpace-class}} object
#' @param what the name of the parameter. If missing returns all parameters.
#' @return the parameter as a \code{character}
#' @include Classes.R
#' @seealso \code{\link{VoxelSpace-class}};
#' @examples
#' # load a voxel file
#' vxsp <- readVoxelSpace(system.file("extdata", "tls_sample.vox", package = "AMAPVox"))
#' # show parameters name
#' names(getParameter(vxsp))
#' # retrieve 'mincorner' parameter
#' getParameter(vxsp, "mincorner")
#' # all parameters
#' getParameter(vxsp)
#' @export
setGeneric("getParameter",
           function(vxsp, what){standardGeneric ("getParameter")})

#' Gets the x, y, z coordinates of the voxel space bottom left corner.
#'
#' @docType methods
#' @rdname getMinCorner
#' @description Gets the x, y, z coordinates of the voxel space bottom left
#'   corner.
#' @param vxsp the \code{\link{VoxelSpace-class}} object.
#' @return the x, y, z coordinates of the voxel space bottom left corner, as a
#'   numerical vector.
#' @examples
#' # load a voxel file
#' vxsp <- readVoxelSpace(system.file("extdata", "tls_sample.vox", package = "AMAPVox"))
#' # retrieve 'min_corner' parameter
#' getMinCorner(vxsp)
#' @export
setGeneric("getMinCorner",
           function(vxsp){standardGeneric ("getMinCorner")})

#' Gets the x, y, z coordinates of the voxel space top right corner.
#'
#' @docType methods
#' @rdname getMaxCorner
#' @description Gets the x, y, z coordinates of the voxel space top right
#'   corner.
#' @param vxsp the \code{\link{VoxelSpace-class}} object.
#' @return the x, y, z coordinates of the voxel space top right corner, as a
#'   numerical vector.
#' @examples
#' # load a voxel file
#' vxsp <- readVoxelSpace(system.file("extdata", "tls_sample.vox", package = "AMAPVox"))
#' # retrieve 'max_corner' parameter
#' getMaxCorner(vxsp)
#' @export
setGeneric("getMaxCorner",
           function(vxsp){standardGeneric ("getMaxCorner")})

#' Gets the elemental size of a voxel (dx, dy, dz) in meter.
#'
#' @docType methods
#' @rdname getResolution
#' @description Gets the elemental size of a voxel (dx, dy, dz) in meter.
#' @param vxsp the \code{\link{VoxelSpace-class}} object.
#' @return the size of the voxel in meter, as a numerical vector.
#' @examples
#' # load a voxel file
#' vxsp <- readVoxelSpace(system.file("extdata", "tls_sample.vox", package = "AMAPVox"))
#' # retrieve 'resolution' parameter
#' getResolution(vxsp)
#' @export
#' @export
setGeneric("getResolution",
           function(vxsp){standardGeneric ("getResolution")})

#' Gets the x, y, z coordinates of a given voxel.
#'
#' @docType methods
#' @rdname getPosition
#' @description Gets the x, y, z coordinates of the voxel center. If the voxel
#'   parameter is missing, it returns the positions of all the voxels in the
#'   voxel space.
#' @param vxsp a \code{\link{VoxelSpace-class}} object.
#' @param vx either a subset of voxels from the \code{\link{VoxelSpace-class}}
#'     data.table or a voxel index as a \code{c(i, j, k)} vector.
#' @return the x, y, z coordinates of the voxel center.
#' @examples
#' # load a voxel file
#' vxsp <- readVoxelSpace(system.file("extdata", "tls_sample.vox", package = "AMAPVox"))
#'
#' # get position of voxel(i=0, j=0, k=0)
#' getPosition(vxsp, c(0, 0, 0))
#'
#' # get position of voxels 1 to 10 in the data.table
#' getPosition(vxsp, vxsp@data[1:10,])
#'
#' # get positions of every voxel
#' getPosition(vxsp)
#' @export
setGeneric("getPosition",
           function(vxsp, vx){standardGeneric ("getPosition")})
