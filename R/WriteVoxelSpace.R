#' Write a voxel file
#'
#' @docType methods
#' @rdname writeVoxelSpace
#' @description write a voxel file out of a \code{\link{VoxelSpace-class}}
#'   object.
#' @param vxsp the object of class VoxelSpace to write
#' @param f a character string naming a file.
#' @include Classes.R
#' @seealso \code{\link{readVoxelSpace}}
#' @examples
#' \dontrun{
#' # load a voxel file
#' vxsp <- readVoxelSpace(system.file("extdata", "tls_sample.vox", package = "AMAPVox"))
#' # set max PAD to 5
#' vxsp@data[, PadBVTotal:=sapply(PadBVTotal, min, 5)]
#' # write updated voxel file in temporary file
#' writeVoxelSpace(vxsp, tempfile("pattern"="amapvox_", fileext=".vox"))
#' }
#' @export
writeVoxelSpace <- function(vxsp, f){

  stopifnot(is.VoxelSpace(vxsp))

  # write header
  conn <- file(f, open="w")
  writeLines("VOXEL SPACE", conn)
  writeLines(printHeader(vxsp), conn)
  close(conn)

  # write voxels
  suppressWarnings(
    data.table::fwrite(vxsp@data,
                     f,
                     row.names=FALSE,
                     col.names=TRUE,
                     na="NaN",
                     sep=" ",
                     append=TRUE,
                     quote=FALSE,
                     scipen = 999)
  )
  cat("Saved voxel file ", f, "[OK]")
#  options(scipen = scipen_o, digits = digits_o)
}

# format voxel file header
# returns a vector of formatted parameters "#key:value"
printHeader <- function(vxsp) {

  # list parameters, discard nline & columnNames that are internal to package
  parameters <- vxsp@header[!(names(vxsp@header)
                                  %in% c("nline", "columnNames"))]
  # index of numeric vector parameters
  pVec <- which(sapply(parameters,
                       function(p) is.numeric(p) && length(p) > 1) > 0)
  # format numeric vectors and concatenate with remaining parameters
  parameters <- c(parameters[-pVec],
                  sapply(parameters[pVec], .formatNumericVector))
  # renamed some parameters
  names(parameters) <- sapply(
    names(parameters),
    function(p) switch(p,
                       mincorner = "min_corner",
                       maxcorner = "max_corner",
                       resolution = "res",
                       dim = "split",
                       p))

  # return a vector of parameters formatted as "#key:value"
  return ( sort(paste0("#", paste(names(parameters), parameters, sep=":"))) )
}

# format numeric vector in readable format for AMAPVox
# returns x formatted as "(x1, x2, ..., xn)"
.formatNumericVector <- function(x) {
  return(paste0("(", paste(x, collapse = ", "), ")"))
}
