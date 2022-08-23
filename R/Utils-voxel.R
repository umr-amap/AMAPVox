
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
            i <- j <- k <- NULL # trick to avoid "no visible binding" note
            for (var in var.cleared) {
              if (var %in% names(vxsp)) {
                vxsp@data[vx, (var):=0, on=list(i, j, k)]
              }
            }
            # special case for transmittance, set to 1
            if ("transmittance" %in% names(vxsp)) {
              vxsp@data[vx, ("transmittance"):= 1, on=list(i, j, k)]
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

#' Voxel layer to raster
#'
#' @docType methods
#' @rdname toRaster
#'
#' @description Converts a voxel space (i, j) layer into a
#' [`terra::SpatRaster-class`] object.
#'
#' @param vxsp a [`VoxelSpace-class`] object.
#' @param vx a voxel space horizontal slice. A data.table with `i, j` columns
#' and least one additional variable, the value of the raster layer. Every
#' column beside i and j will be converted into a raster layer.
#'
#' @return a [`terra::SpatRaster-class`] object.
#'
#' @examples
#' \dontrun{
#' vxsp <- readVoxelSpace(system.file("extdata", "tls_sample.vox", package = "AMAPVox"))
#' library(terra)
#'
#' # CHM, DEM and PAI as raster
#' plot(toRaster(vxsp, merge(canopyHeight(vxsp), groundElevation(vxsp), all = T)))
#'
#' # PAI
#' vxsp <- plantAreaDensity(vxsp)
#' pai <- plantAreaIndex(vxsp, type = "xy", pattern.pad = "pad_transmittance")
#' plot(toRaster(vxsp, pai))
#'
#' # sampling intensity at 2 meters
#' plot(toRaster(vxsp, vxsp@data[ground_distance == 2.25, .(i, j, nbSampling)]))
#' }
#'
#' @export
toRaster <- function(vxsp, vx) {

  # check for terra package
  if (!requireNamespace("terra", quietly = TRUE)) {
    stop(
      "Package \"terra\" must be installed to convert voxel layer into raster.",
      "\n",
      "> install.packages(\"terra\")",
      call. = FALSE)
  }

  # must be a voxel space
  stopifnot(is.VoxelSpace(vxsp))

  # vx must be data.table with i, j columns
  stopifnot(any(class(vx) == "data.table"))
  stopifnot(c("i", "j") %in% names(vx))

  # there must be a third column beside i, j
  stopifnot(ncol(vx) >= 3)

  # i, j coordinates must be unique (no vertical dimension)
  i <- j <- N <- .N <- NULL # trick to get rid of R CMD check warning with data.table
  if (vx[, .N, by=list(i, j)][, all(N > 1)])
    stop("The `vx` layer must have unique (i, j) coordinates.")

  # layers name
  layers.name <- colnames(vx)[! colnames(vx) %in% c("i", "j")]

  nx <- dim(vxsp)[1]
  ny <- dim(vxsp)[2]
  xmin <- AMAPVox::getMinCorner(vxsp)[1]
  ymin <- AMAPVox::getMinCorner(vxsp)[2]
  xmax <- AMAPVox::getMaxCorner(vxsp)[1]
  ymax <- AMAPVox::getMaxCorner(vxsp)[2]

  # terra::raster and AMAPVox voxel space does not have same convention for
  # plot origin, so reorder cell index
  ind <- 1 + vx[["i"]] + nx * (ny - vx[["j"]] - 1)

  # raster layers list
  r <- list()

  # loop on layers
  for (layer.name in layers.name) {
    layer <- rep(NA, length.out = nx * ny)
    layer[ind] <- vx[[layer.name]]
    r[[layer.name]] <- terra::rast(nrows = ny, ncols = nx,
                xmin = xmin, xmax = xmax, ymin = ymin, ymax = ymax,
                vals = layer)
  }

  # stack layers into a single raster
  return ( terra::rast(r))
}

#' Merge two voxel spaces
#'
#' @docType methods
#' @rdname merge
#'
#' @description Merge of two [`VoxelSpace-class`] object.
#'   Voxel spaces must have same sptial extension and resolution, and some
#'   shared column names.
#'
#'   ## Merging modes
#'
#'   Variables `i, j, k & ground_distance` are merged.
#'
#'   Variables `nbEchos, nbSampling, lgTotal, bsEntering, bsIntercepted,
#'   bsPotential, weightedEffectiveFreepathLength & weightedFreepathLength`
#'   are summed-up.
#'
#'   Variables `sdLength, angleMean and distLaser` are weighted means with
#'   `nbSampling` (the number of pulses) as weights.
#'
#'   Attenuation FPL variables (`attenuation_FPL_biasedMLE,
#'   attenuation_FPL_biasCorrection, attenuation_FPL_unbiasedMLE) & lMeanTotal`
#'   are calculated analytically.
#'
#'   Transmittance and attenuation variables (except the FPL attenuation
#'   variables listed above) are weighted means with bsEntering as weights.
#'
#'   Any other variables will not be merged. In particular PAD variables
#'   are not merged and should be recalculated with
#'   [plantAreaDensity()] on the merged voxel space.
#'   E.g:`vxsp <- plantAreaDensity(merge(vxsp1, vxsp2))`
#'
#'   ## Merging multiple voxel spaces
#'
#'   Merging several voxel spaces works as follow : vxsp1 and vxsp2 merged
#'   into vxsp12. vxsp12 & vxsp3 merged into vxsp123, etc. The process can be
#'   synthesized with the [Reduce()] function.
#'   ```r
#'   vxsp <- Reduce(merge, list(vxsp1, vxsp2, vxsp3))
#'   ```
#'
#' @param x,y [`VoxelSpace-class`] objects to be merged.
#' @param ... Not used
#' @return A merged [`VoxelSpace-class`] object.
#'
#' @examples
#' # merge same voxel space to confirm merging behavior
#' vxsp1 <- readVoxelSpace(system.file("extdata", "tls_sample.vox", package = "AMAPVox"))
#' vxsp2 <- readVoxelSpace(system.file("extdata", "tls_sample.vox", package = "AMAPVox"))
#' vxsp <- merge(vxsp1, vxsp2)
#' all(vxsp$nbSampling == vxsp1$nbSampling + vxsp2$nbSampling)
#'
#' # with PAD
#' vxsp <- plantAreaDensity(merge(vxsp1, vxsp2), pulse.min = 1)
#' all((vxsp$pad_transmittance - vxsp1$PadBVTotal) < 1e-7) # equal at float precision
#'
#' @export
merge.VoxelSpace <- function(x, y, ...) {

  # must be a voxel space
  stopifnot(is.VoxelSpace(x), is.VoxelSpace(y))

  # same spatial extension
  stopifnot(getMinCorner(x) == getMinCorner(y))
  stopifnot(getMaxCorner(x) == getMaxCorner(y))
  stopifnot(getVoxelSize(x) == getVoxelSize(y))

  # shared variables
  variables.merged <- intersect(names(x), names(y))
  if (!all(c("i", "j", "k", "nbSampling") %in% variables.merged)) {
    stop("i, j, k & nbSampling variables are mandatory for merging voxel spaces.")
  }

  # Discarded variables
  x.discarded <- names(x)[which(!names(x) %in% variables.merged)]
  y.discarded <- names(y)[which(!names(y) %in% variables.merged)]
  if (length(x.discarded) == 1)
    warning(paste("Variable", x.discarded,
                  "from x is not in y. Discarded from merging."))
  else if (length(x.discarded) > 1)
    warning(paste("Variables", x.discarded,
                  "from x are not in y. Discarded from merging."))
  if (length(y.discarded) == 1)
    warning(paste("Variable", y.discarded,
                  "from y is not in x. Discarded from merging."))
  else if (length(y.discarded) > 1)
    warning(paste("Variables", y.discarded,
                  "from y are not in x. Discarded from merging."))

  # raw merge
  .SD <- .SDcols <- NULL # trick to get rid of `no visible binding` note
  vx.raw <- data.table::merge.data.table(x@data[, .SD, .SDcols=variables.merged],
                  y@data[, .SD, .SDcols=variables.merged],
                  all = TRUE, by = c("i", "j", "k"),
                  suffixes = c(".x", ".y"))

  i <- j <- k <- NULL # trick to get rid of `no visible binding` note
  vx.merged = vx.raw[, list(i, j, k)]

  # list of predifined custom merge
  variable.custom <- c("i", "j", "k", "ground_distance", "lMeanTotal",
                       "attenuation_FPL_biasedMLE",
                       "attenuation_FPL_biasCorrection",
                       "attenuation_FPL_unbiasedMLE")

  # ground distance
  ground_distance.y <- ground_distance <- NULL # trick to get rid of `no visible binding` note
  vx.merged[["ground_distance"]] <- vx.raw[["ground_distance.x"]]
  ind.grd.y <- vx.raw[!is.na(ground_distance.y), which = TRUE]
  vx.merged[ind.grd.y,
            ground_distance:=vx.raw[["ground_distance.y"]][ind.grd.y]]

 # weighted mean function for merging
  wmean <- function(x) {
    return ( stats::weighted.mean(x[1:2], x[3:4], na.rm = TRUE))
  }

  # merge variables one at a time
  for (variable in
       variables.merged[which(!variables.merged %in% variable.custom)]) {

    if (variable %in% c("nbEchos", "nbSampling", "lgTotal",
                        "bsPotential", "bsEntering", "bsIntercepted",
                        "weightedEffectiveFreepathLength",
                        "weightedFreepathLength")) {
      # sum
      vx.merged[[variable]] <- vx.raw[, apply(.SD, 1, sum, na.rm=TRUE),
                                         .SDcols=paste0(variable, c(".x", ".y"))]
    } else if (variable %in% c("sdLength", "angleMean", "distLaser")) {
      # weighted mean on number of sampling
      xy <- cbind(vx.raw[[paste0(variable, ".x")]],
                 vx.raw[[paste0(variable, ".y")]],
                 vx.raw[["nbSampling.x"]],
                 vx.raw[["nbSampling.y"]])
      vx.merged[[variable]] <- apply(xy, 1, wmean)
    } else if (grepl("^(att|tra)", variable)) {
      # weighted mean on entering beam surface for attenuation/transmittance
      if ("bsEntering" %in% variables.merged) {
        xy <- cbind(vx.raw[[paste0(variable, ".x")]],
                   vx.raw[[paste0(variable, ".y")]],
                   vx.raw[["bsEntering.x"]],
                   vx.raw[["bsEntering.y"]])
        vx.merged[[variable]] <- apply(xy, 1, wmean)
      } else {
        warning("`", variable,"` cannot be merged without `bsEntering` variable.")
      }
    }
    else {
      # unknown or user-defined variable, not merged
      warning("Variable `", variable, "` does not have predefined merging mode. Discarded from merging.")
    }
  }

  # lMeanTotal = lgTotal / nbSampling
  if (all(c("lMeanTotal", "lgTotal") %in% variables.merged)) {
    lgTotal <- nbSampling <- lMeanTotal <- NULL # trick to get rid of `no visible binding` note
    vx.merged[["lMeanTotal"]] <- vx.merged[, lgTotal / nbSampling]
    vx.merged[is.infinite(lMeanTotal), lMeanTotal:=NA]
  } else if ("lMeanTotal" %in% variables.merged) {
    warning("`lMeanTotal` cannot be merged without `lgTotal` variable.")
  }


  # biased attenuation FPL (free path length)
  if ("attenuation_FPL_biasedMLE" %in% variables.merged) {
    if (all(c("bsIntercepted", "weightedEffectiveFreepathLength")
            %in% variables.merged)) {
      attenuation_FPL_biasedMLE <- bsIntercepted <- weightedEffectiveFreepathLength <- NULL # trick to get rid of `no visible binding` note
      vx.merged[["attenuation_FPL_biasedMLE"]] <- vx.merged[, bsIntercepted / weightedEffectiveFreepathLength]
      vx.merged[is.infinite(attenuation_FPL_biasedMLE), attenuation_FPL_biasedMLE:=NA]
    } else {
      warning("`attenuation_FPL_biasedMLE` cannot be merged without
              `bsIntercepted` and `weightedEffectiveFreepathLength` variables.")
    }
  }

  # attenuation FPL correction factor
  if ("attenuation_FPL_biasCorrection" %in% variables.merged) {
    if ("weightedEffectiveFreepathLength" %in% variables.merged) {
      xx <- vx.raw[["attenuation_FPL_biasCorrection.x"]] *
        vx.raw[["weightedEffectiveFreepathLength.x"]]^2 *
        vx.raw[["nbSampling.x"]]
      yy <- vx.raw[["attenuation_FPL_biasCorrection.y"]] *
        vx.raw[["weightedEffectiveFreepathLength.y"]]^2 *
        vx.raw[["nbSampling.y"]]
      attenuation_FPL_biasCorrection <- weightedEffectiveFreepathLength <- nbSampling <- NULL # trick to get rid of `no visible binding` note
      vx.merged[["attenuation_FPL_biasCorrection"]] <-
        apply(cbind(xx, yy), 1, sum, na.rm = TRUE) / vx.merged[, weightedEffectiveFreepathLength^2 * nbSampling]
      vx.merged[is.infinite(attenuation_FPL_biasCorrection), attenuation_FPL_biasCorrection:=NA]
    } else {
      warning("`attenuation_FPL_biasedMLE` cannot be merged without
              `weightedEffectiveFreepathLength` variable")
    }
  }

  # unbiased attenuation FPL
  if ("attenuation_FPL_unbiasedMLE" %in% variables.merged) {
    if (all(c("attenuation_FPL_biasedMLE", "attenuation_FPL_biasCorrection")
            %in% variables.merged)) {
      attenuation_FPL_biasedMLE <- attenuation_FPL_biasCorrection <- NULL # trick to get rid of `no visible binding` note
      vx.merged[["attenuation_FPL_unbiasedMLE"]] <- vx.merged[, attenuation_FPL_biasedMLE - attenuation_FPL_biasCorrection]
    } else {
      warning("`attenuation_FPL_unbiasedMLE` cannot be merged without
              `attenuation_FPL_biasedMLE` & `attenuation_FPL_biasCorrection` variables.")
    }
  }

  # new VoxelSpace object
  vxsp.merged <- new(Class=("VoxelSpace"))
  vxsp.merged@header <- x@header
  vxsp.merged@data <- vx.merged
  # return merged voxel space
  return ( vxsp.merged )
}

