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
#' vxsp <- readVoxelSpace(system.file("extdata", "als_sample.vox", package = "AMAPVox"))
#' # set max PAD to 5
#' vxsp@voxels[, PadBVTotal:=sapply(PadBVTotal, min, 5)]
#' # write updated voxel file in temporary file
#' writeVoxelSpace(vxsp, tempfile("pattern"="amapvox_", fileext=".vox"))
#' }
#' @export
writeVoxelSpace <- function(vxsp, f){

  stopifnot(is.VoxelSpace(vxsp))

  # write header
  conn <- file(f, open="w")
  writeLines("VOXEL SPACE", conn)
  writeLines(paste0("#",
                    paste(names(vxsp@parameters$parameters),
                          vxsp@parameters$parameters, sep=":")),
             conn)
  close(conn)

  # write voxels
  suppressWarnings(
    utils::write.table(vxsp@voxels,
                     f,
                     row.names=FALSE,
                     col.names=TRUE,
                     na="NaN",
                     sep=" ",
                     append=TRUE,
                     quote=FALSE)
  )
  cat("Saved voxel file ", f, "[OK]")
}
