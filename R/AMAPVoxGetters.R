#' @rdname getParameter
setMethod("getParameter", signature(object="VoxHeader", what="character"),
          function(object, what) {
            stopifnot(
              sum(!is.na(str_match(
                names(object@header@parameters),
                paste0("^", what, "$")))) == 1)
            return ( object@parameters[what] )
          })

#' @rdname getParameter
setMethod("getParameter", signature(object="VoxelSpace", what="character"),
          function(object, what) {
            return ( callGeneric(object@header, what) )
          })

#' @rdname getMinCorner
setMethod("getMinCorner", "VoxelSpace",
          function(voxelSpace) {
            return ( voxelSpace@header@mincorner )
          })

#' @rdname getMaxCorner
setMethod("getMaxCorner", "VoxelSpace",
          function(voxelSpace) {
            return ( voxelSpace@header@maxcorner )
          })

#' @rdname getResolution
setMethod("getResolution", "VoxelSpace",
          function(voxelSpace) {
            return ( voxelSpace@header@resolution )
          })

#' @rdname getPosition
setMethod("getPosition", signature(voxelSpace="VoxelSpace", voxel="vector"),
          function(voxelSpace, voxel) {

            # 3 coordinates i, j, k
            stopifnot(length(voxel) == 3)
            # i, j, k must be integers
            stopifnot(as.integer(voxel) == voxel)
            # check i, j, k ranges
            stopifnot(all((voxel >= 0) & (voxel < voxelSpace@header@split)))

            position <- vector(mode="numeric", length=3)
            names(position) <- c("x", "y", "z")
            position <- voxelSpace@header@mincorner
                        + voxel * voxelSpace@header@resolution
            return ( position )
          })

#' @rdname getPosition
setMethod("getPosition", signature(voxelSpace="VoxelSpace", voxel="list"),
          function(voxelSpace, voxel) {

            # ensure existence of i, j, k
            stopifnot(all(c("i", "j", "k") %in% colnames(voxelSpace@voxels)))

            return (
              callGeneric(voxelSpace, unlist(voxel[ , c("i", "j", "k")])) )
          })

#' @rdname getPosition
setMethod("getPosition", signature(voxelSpace="VoxelSpace", voxel="missing"),
          function(voxelSpace, voxel) {

            return ( voxelSpace@header@mincorner
                     + voxelSpace@voxels[, c("i", "j", "k")]
                     * voxelSpace@header@resolution )
          })


