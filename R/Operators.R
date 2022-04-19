#' Extract or Replace Parts of a VoxelSpace Object
#'
#' @description Operators acting on \code{VoxelSpace} object. If user attempts
#'
#' @param x A \code{VoxelSpace} object
#' @param name A literal character string or a name (possibly backtick quoted).
#' @param value typically an array-like R object of a similar class as x.
#' @param i string, name of elements to extract.
#' @param j Unused.
#' @param \dots Unused.
#'
#' @name Extract
#' @examples
#' # load a voxel file
#' vxsp <- readVoxelSpace(system.file("extdata", "tls_sample.vox", package = "AMAPVox"))
#'
#' # extract columns or header parameters
#' vxsp$nbSampling
#' vxsp[["i"]]
#' vxsp[["mincorner"]]
#'
#' \dontrun{
#' # add new column
#' vxsp[["pad_capped"]] <- ifelse(vxsp$PadBVTotal > 0.5, 0.5, vxsp$PadBVTotal)
#' # update header parameter
#' vxsp[["max_pad"]] <- 0.5
#' }
#'
NULL

#' @export
#' @rdname Extract
setMethod("$", "VoxelSpace", function(x, name) { x[[name]] })

#' @export
#' @rdname Extract
setMethod("[[", c("VoxelSpace", "ANY", "missing"), function(x, i, j, ...) {

  if (is.character(i) && !i %in% names(x@data))
    return(x@header[[i]])

  return(x@data[[i]])
})

#' @export
#' @rdname Extract
setMethod("$<-", "VoxelSpace", function(x, name, value)
{
  x[[name]] <- value
  return(x)
})

#' @export
#' @aliases [[<-,VoxelSpace,ANY,missing-method
#' @rdname Extract
setMethod("[[<-", c("VoxelSpace", "ANY", "missing", "ANY"),  function(x, i, j, value)
{

  # replace header parameter
  if (i %in% names(x@header))  {
    x@header[[i]] <- value
    return(x)
  }

  # forbid operation on grid coordinates
  if (i %in% c("i", "j", "k")) {
    stop("Direct modification of grid index is not allowed",
         "Please use AMAPVox::crop function instead.")
  }

  # replace or add new column in data.table
  x@data[[i]] <- value
  return(x)

})

#' @export
#' @rdname Extract
setMethod("$<-", "VoxelSpace", function(x, name, value)
{
  x[[name]] <- value
  return(x)
})
