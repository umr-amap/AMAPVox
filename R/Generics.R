#' Tools inherited from base R for VoxelSpace object.
#'
#' @description Tools inherited from base R for \code{\link{VoxelSpace-class}}
#' objects.
#'
#' @section Note on `length.VoxelSpace`:
#'     AMAPVox allows to discard empty voxels in the voxel file. In such case
#'     \code{length.VoxelSpace} will return the expected number of voxels as if
#'     none were missing. As a  consequence the number of voxels stored in the
#'     \code{\link{VoxelSpace-class}} object may be inferior to the returned
#'     value, namely \code{nrow(x) <= length(x)}
#'
#' @param x a \code{\link{VoxelSpace-class}} object.
#' @param object a \code{\link{VoxelSpace-class}} object.
#' @param \dots further arguments passed to `print` function.
#'
#' @name tools
#' @rdname tools
NULL

#' @rdname tools
#' @export
setMethod ("show",
           signature(object = "VoxelSpace"),
           function(object) showVoxelSpace(object))

#' @rdname tools
#' @export
print.VoxelSpace <- function(x, ...) showVoxelSpace(x, ...)

#' @rdname tools
#' @export
length.VoxelSpace <- function(x) return (prod(x@header$dim))

#' @rdname tools
#' @export
dim.VoxelSpace <- function(x) return (x@header$dim)

#' @rdname tools
#' @export
is.VoxelSpace <- function (x) is(x, "VoxelSpace")

#' @export
#' @rdname tools
setMethod ("ncol",
           signature(x = "VoxelSpace"),
           function(x) ncol(x@data))

#' @export
#' @rdname tools
setMethod ("nrow",
           signature(x = "VoxelSpace"),
           function(x) nrow(x@data))
#' @export
#' @rdname tools
nrow.VoxelSpace <- function(x) return(nrow(x@data))

#' @export
#' @rdname tools
names.VoxelSpace <- function(x) return(names(x@data))

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
#' @rdname getVoxelSize
#' @description Gets the elemental size of a voxel (dx, dy, dz) in meter.
#' @param vxsp the \code{\link{VoxelSpace-class}} object.
#' @return the size of the voxel in meter, as a numerical vector.
#' @examples
#' # load a voxel file
#' vxsp <- readVoxelSpace(system.file("extdata", "tls_sample.vox", package = "AMAPVox"))
#' # retrieve voxel size
#' getVoxelSize(vxsp)
#' @export
#' @export
setGeneric("getVoxelSize",
           function(vxsp){standardGeneric ("getVoxelSize")})

#' Gets the x, y, z coordinates of a given voxel.
#'
#' @docType methods
#' @rdname getPosition
#' @description Gets the x, y, z coordinates of the voxel center. If the voxel
#'   parameter is missing, it returns the positions of all the voxels in the
#'   voxel space.
#' @param vxsp a \code{\link{VoxelSpace-class}} object.
#' @param vx (i, j, k) voxel coordinates as a [data.table::data.table] with
#' i, j, k columns, a vector (i, j, k) or a matrix with i, j, k columns.
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

#' Clear voxel
#'
#' @docType methods
#' @rdname clear
#'
#' @description Clear a set of voxels. Clearing means that the state variables
#' of the selected voxels are altered as if they were *clear* of any vegetation.
#' Namely:
#' \itemize{
#' \item{number of echo set to zero}
#' \item{intercepted beam surface set to zero (if variable is outputed)}
#' \item{plant area density set to zero (if variable is outputed)}
#' \item{transmittance set to one (if variable is outputed)}
#' \item{any attenuation variable set to zero}
#' }
#' Other state variables such as sampling intensity, mean angle, entering beam
#' surface, etc. are unaltered. A cleared voxel is not the same as an unsampled
#' voxel (not "crossed" by any beam).
#'
#' @param vxsp a \code{\link{VoxelSpace-class}} object.
#' @param vx (i, j, k) voxel coordinates as a [data.table::data.table] with
#' i, j, k columns, a vector (i, j, k) or a matrix with i, j, k columns.
#'
#' @examples
#' # load a voxel file
#' vxsp <- readVoxelSpace(system.file("extdata", "tls_sample.vox", package = "AMAPVox"))
#' # clear 1st voxel
#' clear(vxsp, c(0, 0, 0)) # clear 1st voxel
#' # clear butterflies
#' clear(vxsp, butterfly(vxsp))
#' # clear voxels with less than two hits
#' clear(vxsp, vxsp@data[nbEchos < 2])
#'
#' @export
setGeneric("clear",
           function(vxsp, vx){standardGeneric ("clear")})

