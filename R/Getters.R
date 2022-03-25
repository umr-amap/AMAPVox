#' @rdname getParameter
setMethod("getParameter", signature(vxsp="VoxelSpace", what="character"),
          function(vxsp, what) {
            stopifnot(
              sum(!is.na(str_match(
                names(vxsp@header),
                paste0("^", what, "$")))) == 1)
            return ( vxsp@header[[what]] )
          })

#' @rdname getParameter
setMethod("getParameter", signature(vxsp="VoxelSpace", what="missing"),
          function(vxsp, what) {

            return ( vxsp@header )
          })

#' @rdname getMinCorner
setMethod("getMinCorner", "VoxelSpace",
          function(vxsp) {
            return ( vxsp@header$mincorner )
          })

#' @rdname getMaxCorner
setMethod("getMaxCorner", "VoxelSpace",
          function(vxsp) {
            return ( vxsp@header$maxcorner )
          })

#' @rdname getResolution
setMethod("getResolution", "VoxelSpace",
          function(vxsp) {
            return ( vxsp@header$resolution )
          })

#' @rdname getPosition
setMethod("getPosition", signature(vxsp="VoxelSpace", vx="vector"),
          function(vxsp, vx) {

            # 3 coordinates i, j, k
            stopifnot(length(vx) == 3)
            # i, j, k must be positive integers
            stopifnot(as.integer(vx) == vx)
            stopifnot(all(vx >=0))
            # check i, j, k ranges
            stopifnot(all((vx >= 0) & (vx < vxsp@header$split)))

            return (
              callGeneric(vxsp,
                          data.table::data.table(i=vx[1], j=vx[2], k=vx[3])))
          })

#' @rdname getPosition
setMethod("getPosition", signature(vxsp="VoxelSpace", vx="data.table"),
          function(vxsp, vx) {

            # ensure existence of i, j, k
            stopifnot(all(c("i", "j", "k") %in% colnames(vx)))

            # extract i, j, k
            pos <- vx[, c("i", "j", "k")]
            # min corner and resolution as local variables
            minc <- vxsp@header$mincorner
            res <- vxsp@header$resolution
            # function for calculating the position
            calcPos <- function(index, coord) minc[coord] + index * res[coord]
            # compute x, y, z
            i <- j <- k <- x <- y <- z <- NULL # due to NSE notes in R CMD check
            pos <- pos[, x:=calcPos(i, "x")][, y:=calcPos(j, "y")][, z:=calcPos(k, "z")][, c("x", "y", "z")]
            # return positions as data.table
            return ( pos )
          })

#' @rdname getPosition
setMethod("getPosition", signature(vxsp="VoxelSpace", vx="missing"),
          function(vxsp, vx) {

            return (
              callGeneric(vxsp, vxsp@data[ , c("i", "j", "k")]))
          })


