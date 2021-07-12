#' @rdname getParameter
setMethod("getParameter", signature(object="VoxHeader", what="character"),
          function(object, what) {
            stopifnot(
              sum(!is.na(str_match(
                names(object@parameters),
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
            # i, j, k must be positive integers
            stopifnot(as.integer(voxel) == voxel)
            stopifnot(all(voxel >=0))
            # check i, j, k ranges
            stopifnot(all((voxel >= 0) & (voxel < voxelSpace@header@split)))

            return (
              callGeneric(voxelSpace, data.table::data.table(i=voxel[1], j=voxel[2], k=voxel[3])))
          })

#' @rdname getPosition
setMethod("getPosition", signature(voxelSpace="VoxelSpace", voxel="data.table"),
          function(voxelSpace, voxel) {

            # ensure existence of i, j, k
            stopifnot(all(c("i", "j", "k") %in% colnames(voxel)))

            # extract i, j, k
            pos <- voxel[, c("i", "j", "k")]
            # min corner and resolution as local variables
            minc <- voxelSpace@header@mincorner
            res <- voxelSpace@header@resolution
            # function for calculating the position
            calcPos <- function(index, coord) minc[coord] + index * res[coord]
            # compute x, y, z
            i = j = k = x = y = z = NULL # due to NSE notes in R CMD check
            pos <- pos[, x:=calcPos(i, "x")][, y:=calcPos(j, "y")][, z:=calcPos(k, "z")][, c("x", "y", "z")]
            # return positions as data.table
            return ( pos )
          })

#' @rdname getPosition
setMethod("getPosition", signature(voxelSpace="VoxelSpace", voxel="missing"),
          function(voxelSpace, voxel) {

            return (
              callGeneric(voxelSpace, voxelSpace@voxels[ , c("i", "j", "k")]))
          })


