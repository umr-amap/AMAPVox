#' Computes Plant Area Density (PAD)
#'
#' @description Computes Plant Area Density either from transmittance or
#' attenuation coefficient estimates.
#' @param vxsp the object of class VoxelSpace
#' @param lad the name of the probability density function of the leaf angle
#' distribution. One of `AMAPVox:::leafAngleDistribution`.
#' @param angle.name the name of the mean angle variable in the VoxelSpace
#' object.
#' @param variable.name the name of the transmittance/attenuation variables in
#' the VoxelSpace object. Transmittance variables are expected to start with
#' "tra" and attenuation variables with "att".
#' @param pad.max a float, the maximal PAD value
#' @param pulse.min an integer, the minimal number of pulses in a voxel for
#' computing the PAD. PAD set to NA otherwise.
#' @param ... additional parameters which will be passed to the leaf angle
#' distribution functions. Details in [AMAPVox::computeG].
#' @seealso [AMAPVox::computeG]
#' @references VINCENT, Gregoire; PIMONT, François; VERLEY, Philippe, 2021,
#' "A note on PAD/LAD estimators implemented in AMAPVox 1.7",
#' \url{https://doi.org/10.23708/1AJNMP}, DataSuds, V1
#' @export
plantAreaDensity <- function(vxsp, lad = "spherical",
                             angle.name = "angleMean",
                             variable.name = c("transmittance",
                                               "attenuation_FPL_unbiasedMLE",
                                               "attenuation_PPL_MLE"),
                             pad.max = 5, pulse.min = 5,
                             ...) {

  # must be a voxel space
  stopifnot(is.VoxelSpace(vxsp))

  # check leaf angle distribution
  stopifnot(lad %in% leafAngleDistribution)

  # angle variable must exist
  stopifnot(angle.name %in% colnames(vxsp@data))

  # transmittance / attenuation variable must exist
  stopifnot(variable.name %in% colnames(vxsp@data))

  # transmittance / attenuation variable must start, by convention, either
  # by "tra" of "att"
  stopifnot(all(grepl("(^tra*)|(^att*)", variable.name)))

  # pointer to voxels data.table
  vx <- vxsp@data

  nbSampling <- padtmp <- NULL # due to NSE notes in R CMD check

  # loop over requested variables
  for (variable in variable.name) {

    # initialize PAD vector with NA
    pad <- rep(NA, length(vxsp))

    # index of voxels such as number of pulses greater than pulse.min
    index <- vx[nbSampling >= pulse.min, which = TRUE]

    # compute G(θ)
    gtheta <- computeG(vx[index, get(angle.name)], lad, ...)

    # compute PAD
    if (grepl("^tra", variable)) {
      # from transmittance
      pad[index] <- log(vx[index, get(variable)]) / (-gtheta)
    } else {
      # from attenuation
      pad[index] <- vx[index, get(variable)] / gtheta
    }

    # cap PAD
    pad[which(pad > pad.max)] <- pad.max

    # add PAD into data.table
    pad.name <- paste0("pad_", variable)
    vxsp@data[, padtmp := pad]
    # overwrite existing column
    if (pad.name %in% colnames(vxsp@data))
      vxsp@data[, (pad.name):=NULL]
    data.table::setnames(vxsp@data, "padtmp", pad.name)
  }
}
