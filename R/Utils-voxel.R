
#' @rdname clear
setMethod("clear", signature(vxsp="VoxelSpace", vx="data.table"),
          function(vxsp, vx) {

            # ensure existence of i, j, k
            stopifnot(all(c("i", "j", "k") %in% colnames(vx)))

            # clear voxels
            var.cleared <- c("nbEchos", "bsIntercepted", "PadBVTotal",
                             "attenuation_FPL_biasedMLE",
                             "attenuation_FPL_biasCorrection",
                             "attenuation_PPL_MLE")
            for (var in var.cleared) {
              if (var %in% names(vxsp)) {
                vxsp@data[vx, (var):=0]
              }
            }
            # special case for transmittance, set to 1
            if ("transmittance" %in% names(vxsp)) {
              vxsp@data[vx, ("transmittance"):= 1]
            }

          })

#' @rdname clear
setMethod("clear", signature(vxsp="VoxelSpace", vx="vector"),
          function(vxsp, vx) {

            # 3 coordinates i, j, k
            stopifnot(length(vx) == 3)
            # i, j, k must be positive integers
            stopifnot(as.integer(vx) == vx)
            stopifnot(all(vx >=0))
            # check i, j, k ranges
            stopifnot(all((vx >= 0) & (vx < dim(vxsp))))

            return (
              callGeneric(vxsp,
                          data.table::data.table(i=vx[1], j=vx[2], k=vx[3])))
          })

#' @rdname clear
setMethod("clear", signature(vxsp="VoxelSpace", vx="matrix"),
          function(vxsp, vx) {

            # 3 columns i, j, k
            stopifnot(ncol(vx) == 3)
            # i, j, k must be integers
            stopifnot(as.integer(vx) == vx)
            # check i, j, k ranges
            stopifnot(
              all(apply(vx, 1, function(vx) (vx >= 0) & (vx < dim(vxsp)))))

            return (
              callGeneric(vxsp,
                          data.table::data.table(i=vx[,1], j=vx[,2], k=vx[,3])))
          })
