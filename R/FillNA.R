#' Fill missing values (NA) with averaged neighboring data
#'
#' @docType methods
#' @rdname fillNA
#'
#' @description Fill missing values of a given variable in a VoxelSpace object
#' with averaged neighboring values.
#'
#' Neighboring values are selected among voxels within a user-defined radius
#' in meter and whose sampling rate (number of pulses that went through the
#' voxel) is above a user-defined threshold. Distance between voxels is the
#' euclidian distance between voxel centers. Fill-value may be capped by
#' user-defined minimal and maximal values.
#'
#' Default radius (if not defined by user) is set to largest dimension of voxel
#' size `max(getResolution(vxsp))`. It guarantees that default neighborhood is
#' isotropic.
#'
#' In some cases, for instance poorly sampled area, neighboring values may all
#' be missing or discarded. A fallback value can be provided to "force fill"
#' suche voxels. An other option is to run again the function with larger
#' radius or lower sampling threshold.
#'
#' @param vxsp a \code{\link{VoxelSpace-class}} object.
#' @param variable.name a character, the name of a variable in the VoxelSpace
#' @param variable.min a numeric, minimal value for the fill values
#' @param variable.max a numeric, maximal value for the fill values
#' @param variable.fallback a numeric, optional fallback value in case no fill
#' value can be estimated from neighboring voxels.
#' @param radius a numeric, the radius in meter that defines the neighborhood of
#' a voxel. The function looks for the voxels whose center is inside a sphere
#' of radius `radius` centered at current voxel center. Default is set to
#' `max(getResolution(vxsp))`
#' @param pulse.min a numeric, minimal sampling intensity (i.e. number of pulses
#' that went through a voxel) to include neighboring voxel in the estimation of
#' the averaged fill value.
#'
#' @examples
#' # read voxel space
#' vxsp <- readVoxelSpace(system.file("extdata", "tls_sample.vox", package = "AMAPVox"))
#' # Randomly add some NA in PAD variable
#' vx <- vxsp@voxels
#' ind <- sample(vx[PadBVTotal > 0, which = TRUE], 3)
#' # print initial values
#' vx[ind, .(i, j, k, PadBVTotal)]
#' vx[ind, PadBVTotal := NA]
#' # fill NA in PAD variable
#' fillNA(vxsp, "PadBVTotal", variable.max = 5)
#' # print filled values
#' vx[ind, .(i, j, k, PadBVTotal)]
#'
#' @export
fillNA <- function(vxsp,
                   variable.name,
                   variable.min = -Inf, variable.max = Inf, variable.fallback,
                   radius, pulse.min = 10) {

  # must be a voxel space
  stopifnot(is.VoxelSpace(vxsp))

  # variable must exist, only one variable at a time
  stopifnot(variable.name %in% colnames(vxsp@voxels),
            length(variable.name) == 1)

  # check variable min & max
  stopifnot(is.numeric(variable.min), length(variable.min) == 1,
            is.numeric(variable.max), length(variable.max) == 1)
  if (!missing(variable.fallback)) stopifnot(is.numeric(variable.fallback),
                                             length(variable.fallback) == 1)

  # radius missing
  # default value set to largest voxel size to make sure there will be
  # neighbors in every direction
  if (missing(radius)) radius <- max(getResolution(vxsp))

  # radius must be a positive float
  stopifnot(is.numeric(radius), length(radius) == 1)

  # minimal number of pulses in voxel
  stopifnot(is.numeric(pulse.min), pulse.min >= 0)

  # pointer to voxels
  vx <- vxsp@voxels

  # extract NA voxels
  vx.na <- vx[is.na(get(variable.name))]

  # extract voxels whose number of pulse >= pulse.min
  vx.pool <- vx[!is.na(get(variable.name)) & nbSampling >= pulse.min]

  # computes max number of neighboring voxels inside sphere(r=radius)
  neighbors.k <- neighbors.max(getResolution(vxsp), radius)
  # identify valid neighbors
  neighbors <- RANN::nn2(data = getPosition(vxsp, vx.pool),
                         query = getPosition(vxsp, vx.na),
                         k = neighbors.k,
                         searchtype = "radius", radius = radius)
  neighbors <- neighbors$nn.idx

  # mean value from neighborhood
  fill.value <- apply(neighbors, 1, function(nghb) vx.pool[nghb, mean(get(variable.name), na.rm = T)])
  # replace NaN by NA (mean function may return NaN if every neighbor is NA)
  fill.value[which(is.nan(fill.value))] <- NA

  # replace NA with fallback value, if provided
  if (!missing(variable.fallback))
    fill.value[which(is.na(fill.value))] <- variable.fallback

  # warn if NA remain
  na.count <- length(which(is.na(fill.value)))
  if (na.count > 0) warning(paste(na.count, " NA left", "\nSet a fallback value or run with larger radius or lower minimal pulse threshold."))

  # cap fill.value
  fill.value[which(fill.value > variable.max)] <- variable.max
  fill.value[which(fill.value < variable.min)] <- variable.min

  # write fill values in data.table
  vxsp@voxels[is.na(get(variable.name)), (variable.name):=fill.value]
}

# Computes number of voxels, given voxel size (in meter), whose centers are
# within sphere of given radius (in meter)
# ~~not~exported~~
neighbors.max <- function(voxel.size, radius) {

  dr <- round(radius / voxel.size)
  nr <- 2 * dr + 1
  x <- voxel.size[1] * rep(seq(-dr[1], dr[1]), each = nr[2] * nr[3])
  y <- voxel.size[2] *rep(rep(seq(-dr[2], dr[2]), each = nr[3]), times = nr[1])
  z <- voxel.size[3] *rep(seq(-dr[3], dr[3]), times = nr[1] * nr[2])
  length(which(sqrt(x^2 + y^2 + z^2) <= radius))
}
