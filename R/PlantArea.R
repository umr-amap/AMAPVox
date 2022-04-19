#' Plant Area Density (PAD)
#'
#' @docType methods
#' @rdname plantAreaDensity
#'
#' @description Computes Plant Area Density either from transmittance or
#' attenuation coefficient estimates.
#' Details of calculation and underlying assumptions can be found online at
#' \doi{10.23708/1AJNMP}.
#' PAD is defind as the plant area per unit volume
#' ( PAD plant area / voxel volume = m^2 / m^3).
#'
#' @param vxsp a \code{\link{VoxelSpace-class}} object.
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
#'
#' @return A voxel space object with the requested PAD variables.
#'
#' @seealso [AMAPVox::computeG]
#' @references VINCENT, Gregoire; PIMONT, François; VERLEY, Philippe, 2021,
#' "A note on PAD/LAD estimators implemented in AMAPVox 1.7",
#' \doi{10.23708/1AJNMP}, DataSuds, V1
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

  # only keep variable name that exists in voxel space
  variables <- variable.name[which(variable.name %in% names(vxsp))]
  if (length(variables) == 0) {
    stop(paste("Variables", paste(variable.name, collapse = ", "), "cannot be found in voxelspace."))
  }

  # transmittance / attenuation variable must start, by convention, either
  # by "tra" of "att"
  stopifnot(all(grepl("(^tra*)|(^att*)", variables)))

  # pointer to voxels data.table
  vx <- vxsp@data

  # loop over requested variables
  for (variable in variables) {

    nbSampling <- padtmp <- NULL # due to NSE notes in R CMD check

    # initialize PAD vector with NA
    pad <- rep(NA, nrow(vxsp))

    # index of voxels such as number of pulses greater than pulse.min
    index <- vx[nbSampling >= pulse.min, which = TRUE]

    # compute G(θ)
    gtheta <- computeG(vxsp[[angle.name]][index], lad, ...)

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
    vxsp[[pad.name]] <- pad
  }

  return ( vxsp )
}

#' Plant Area Index (PAI)
#'
#' @docType methods
#' @rdname plantAreaIndex
#'
#' @description Computes Plant Area Index (PAI) from Plant Area Density (PAD).
#'   PAI is defined as the plant area per unit ground surface area (PAI = plant
#'   area / ground area = m^2 / m^2).
#'
#'   The function can estimate PAI on the whole voxel space or any region of
#'   interest (parameter vx subset of voxels). It can compute PAI from several
#'   perspectives : either an averaged PAI value, a two-dimensions (i, j) PAI
#'   array or vertical profiles either above ground or below canopy.
#'
#' @param vxsp a \code{\link{VoxelSpace-class}} object.
#' @param vx a subset of voxel index. A data.table with `i, j, k` columns.
#'   Missing parameter means whole voxel space.
#' @param type a character vector, the type of PAI profile. \itemize{
#'   \item{`"av"` Averaged value on every voxel} \item{`"ag"` Above ground
#'   vertical profile} \item{`"bc"` Below canopy vertical profile} \item{`"xy"`
#'   Spatial profile} }
#' @param pattern.pad character string containing a [regular expression][regex]
#'   to be matched in the voxel space variable names, for selecting PAD
#'   variables. Typing the name of a specific PAD variable works just fine.
#'
#' @return Returns a list of PAI profiles for requested PAD variables and PAI
#'   types.
#'
#'   ## `av` Averaged PAI
#'
#'   Returns a single value. Calculated as the sum of PAD values multiplied by
#'   voxel volume and divided by ground surface with vegetation.
#'
#'   ## `ag & bc` Above ground and below canopy PAI vertical profile
#'
#'   Returns a vertical profile of PAI values either from ground distance or
#'   canopy depth. Calculated as the averaged PAD values per layer (a layer
#'   being defined by either the distance to ground or canopy level) multiplied
#'   by voxel size along z (equivalent to multiplying PAD by voxel volume and
#'   dividing by voxel ground surface).
#'
#'   ## `xy` Spatial PAI profile
#'
#'   Returns a list a PAI values by i, j index. Calculated as the sum of PAD on
#'   (i, j) column multiplied by voxel size along z (equivalent to multiplying
#'   PAD by voxel volume and dividing by voxel ground surface).
#'
#' @seealso [AMAPVox::plantAreaDensity]
#'
#' @examples
#' vxsp <- readVoxelSpace(system.file("extdata", "tls_sample.vox", package = "AMAPVox"))
#' vxsp <- plantAreaDensity(vxsp)
#' \dontrun{
#' lai <- plantAreaIndex(vxsp)
#' names(lai)
#' library(ggplot2)
#' ggplot(data = lai[["pad_transmittance.pai.ag" ]], aes(x=pai, y=ground_distance)) +
#'   geom_path() + geom_point()
#' }
#' # PAI on a subset
#' ni <- round(dim(vxsp)[1]/2)
#' vx <- vxsp@data[i < ni, .(i, j, k)]
#' lai <- plantAreaIndex(vxsp, vx)
#'
#' @export
plantAreaIndex <- function(vxsp, vx,
                           type = c("av", "ag", "bc", "xy"),
                           pattern.pad = "^pad_*") {

  # must be a voxel space
  stopifnot(is.VoxelSpace(vxsp))

  # vx subset missing == every voxel from vxsp
  if (missing(vx)) {
    i <- j <- k <- NULL # trick to get rid of R CMD check warning with data.table
    vx <- vxsp@data[, list(i, j, k)]
  }

  # vx must be data.table with i, j, k columns
  stopifnot(any(class(vx) == "data.table"))
  stopifnot(c("i", "j", "k") %in% names(vx))

  # type one of av, ag, bc, xy
  stopifnot(is.character(type), type %in% c("av", "ag", "bc", "xy"))

  # pad variables
  pad.variables <- grep(pattern.pad, names(vxsp), value = TRUE)
  if (length(pad.variables) == 0)
    stop(paste("There is not any PAD variables matching pattern", dQuote(pattern.pad, q = FALSE), "in vxsp"))

  # empty pai list
  pai.all <- list()

  dz <- unname(getVoxelSize(vxsp)["z"])

  # loop on PAD variable
  for (pad.variable in pad.variables) {

    # data.table of voxels with required variables for PAI calculation
    i <- j <- k <- ground_distance <- NULL # trick to get rid of R CMD check warning with data.table
    dt <- vxsp@data[vx, list(i, j, k, ground_distance, pad=get(pad.variable))]

    # loop on PAI type
    for (pai.type in type) {
      # handle differernt PAI type
      if ("av" == pai.type) {
        #
        # Averaged PAI
        #
        # number of i, j cells with some vegetation
        pad <- NULL # trick to get rid of R CMD check warning with data.table
        n.cell <- dt[, list(n.cell=sum(pad, na.rm = T) > 0), by=c("i", "j")][, sum(n.cell)]
        # pai = sum(pad) * dxdydz / (n.cell * dxdy) = sum(pad) * dz / n.cell
        pai <- dt[, sum(pad,  na.rm = T) * dz] / n.cell
        #
      } else if ("ag" == pai.type) {
        #
        # Above ground PAI
        #
        grd <- ground(vxsp)
        pai <- merge(dt, grd,
          by = c("i", "j"),
          suffixes = c("", ".grd")
        )
        k <- k.grd <- dk <- ground_distance <- NULL # trick to get rid of R CMD check warning with data.table
        pai <- pai[, list(pai=mean(pad, na.rm = TRUE)),
                  by=list(dk = k - k.grd)][dk >= 0 & !is.na(pai) ][order(dk)]
        pai[, ground_distance := dk * dz][, dk := NULL]
        #
      } else if ("bc" == pai.type) {
        #
        # Below canopy PAI
        #
        cnp <- canopy(vxsp)
        pai <- merge(dt, cnp,
          by = c("i", "j"),
          suffixes = c("", ".cnp"))
        k <- k.cnp <- dk <- canopy_depth <- NULL # trick to get rid of R CMD check warning with data.table
        pai <- pai[, list(pai=mean(pad, na.rm = TRUE)),
                   by=list(dk = k.cnp - k)][dk >= 0 & !is.na(pai) ][order(dk)]
        pai[, canopy_depth := dk * dz][, dk := NULL]
        #
      } else if ("xy" == pai.type) {
        #
        # X, Y PAI
        #
        pad <- NULL # trick to get rid of R CMD check warning with data.table
        pai <- dt[, list(pai=sum(pad, na.rm = T) * dz), by=c("i", "j")]
      }
      # append pai to list
      pai.all[[paste(pad.variable, "pai", pai.type, sep = ".")]] <- pai

    } # end loop pai.type
  } # end loop pad.variable

  if (length(pai.all) == 1)
    return ( pai.all[[1]])
  else
  return ( pai.all )
}
