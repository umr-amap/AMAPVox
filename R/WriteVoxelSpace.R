#' Write a voxel file
#'
#' @docType methods
#' @rdname writeVoxelSpace
#' @description write a voxel file out of a \code{\link{VoxelSpace-class}}
#'   object.
#' @param voxelSpace the object of class VoxelSpace to write
#' @param outputFile The path where to write the voxel file.
#' @include AMAPVoxClasses.R
#' @seealso \code{\link{readVoxelSpace}}
#' @export
writeVoxelSpace <- function(voxelSpace, outputFile){

  stopifnot(is.VoxelSpace(voxelSpace))

  # write header
  conn <- file(outputFile, open="w")
  writeLines("VOXEL SPACE", conn)
  writeLines(paste0("#",
                    paste(names(voxelSpace@header@parameters),
                          voxelSpace@header@parameters, sep=":")),
             conn)
  close(conn)

  # write voxels
  utils::write.table(voxelSpace@voxels,
                     outputFile,
                     row.names=FALSE,
                     col.names=TRUE,
                     na="NaN",
                     sep=" ",
                     append=TRUE,
                     quote=FALSE)
}
