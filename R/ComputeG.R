
#
# leaf angles in radians
# leaf angle = 0 for horizontal leaves
# leaf angle = pi / 2 for vertical leaves
#
# no preferred azimutal direction
#

# planophile probability density of leaf angle distribution
# planophile == horizontal leaves most frequent
# thetaL parameter, the leaf inclination angle in radian [0, pi/2]
dplanophile <- function(thetaL) {
  stopifnot(dplyr::between(thetaL, 0, pi / 2))
  # probability density of eaf angle distribution
  (2 / pi) * (1 + cos(2 * thetaL))
}

# erectophile probability density of leaf angle distribution
# erectophile == vertical leaves most frequent
# thetaL parameter, the leaf inclination angle in radian [0, pi/2]
derectophile <- function(thetaL) {
  stopifnot(dplyr::between(thetaL, 0, pi / 2))
  (2 / pi) * (1 - cos(2 * thetaL))
}

# plagiophile probability density of leaf angle distribution
# plagiophile == oblique leaves most frequent
# thetaL parameter, the leaf inclination angle in radian [0, pi/2]
dplagiophile <- function(thetaL) {
  stopifnot(dplyr::between(thetaL, 0, pi / 2))
  (2 / pi) * (1 - cos(4 * thetaL))
}

# extremophile probability density of leaf angle distribution
# extremophile == oblique leaves are least frequent
# thetaL parameter, the leaf inclination angle in radian [0, pi/2]
dextremophile <- function(thetaL) {
  stopifnot(dplyr::between(thetaL, 0, pi / 2))
  (2 / pi) * (1 + cos(4 * thetaL))
}

# spherical probability density of leaf angle distribution
# spherical == relative frequency of leaf angle is the same as for surface
# elements of a sphere
# thetaL parameter, the leaf inclination angle in radian [0, pi/2]
dspherical <- function(thetaL) {
  stopifnot(dplyr::between(thetaL, 0, pi / 2))
  sin(thetaL)
}

# uniform probability density of leaf angle distribution
# uniform == proportion of leaf angle is the same at any angle
# thetaL parameter, the leaf inclination angle in radian [0, pi/2]
duniform <- function(thetaL) {
  stopifnot(dplyr::between(thetaL, 0, pi / 2))
  2 / pi
}

# ellipsoidal probability density of leaf angle distribution
# ellipsoidal == relative frequency of leaf angle is the same as for surface
# elements of an ellipsoid.
# thetaL parameter, the leaf inclination angle in radian [0, pi/2]
# chi parameter, the ratio horizontal axis over vertical axis. For chi = 1
# the distribution becomes spherical. For chi in [0, 1[, the ellipsoid is a
# a prolate spheroid (like a rugby ball). For chi > 1 the ellipsoid is an
# oblate spheroid (a sphere that bulges at the equator and somewhat squashed
# at the poles).
dellipsoidal <- function(thetaL, chi) {
  stopifnot(dplyr::between(thetaL, 0, pi / 2))
  stopifnot(chi >= 0)
  # chi == 1
  if (chi == 1)
    return(sin(thetaL))
  # chi < 1
  if (chi < 1) {
    epsilon <- sqrt(1 - chi ^ 2)
    lambda <- chi + asin(epsilon) / epsilon
  }
  # chi > 1
  else {
    epsilon <- sqrt(1 - 1 / (chi ^ 2))
    lambda <- chi + log((1 + epsilon) / (1 - epsilon)) / (2 * epsilon * chi)
  }
  2 * (chi ^ 3) * sin(thetaL) / (lambda * (cos(thetaL) ^ 2 +  (chi * sin(thetaL)) ^ 2 ) ^ 2)
}

# two parameters Beta probability density of leaf angle distribution
# Most generic approach from Goal and Strebel (1984) to represent large
# variaty of leaf angle distribution.
# thetaL parameter, the leaf inclination angle in radian [0, pi/2]
# mu and nu parameters, the Beta distribution parameters.
dtwoParamBeta <- function(thetaL, mu, nu) {
  stopifnot(dplyr::between(thetaL, 0, pi / 2))
  stopifnot(all(mu >= 0, nu >= 0))
  t <- 2 * thetaL / pi
  dbeta(t, mu, nu) * (2 / pi)
}

# probability density function of leaf angle distribution
# thetaL parameter, the leaf inclination angle in radian [0, pi/2]
# pdf parameter, the name of the probability density function
# chi the parameter of the ellipsoidal probability density function
# mu & nu the parameters of the two-parameters Beta probability density function
dleaf <- function(thetaL, pdf = "spherical", chi, mu, nu) {

  stopifnot(pdf %in% c("planophile",
                     "erectophile",
                     "extremophile",
                     "plagiophile",
                     "spherical",
                     "uniform",
                     "ellipsoidal",
                     "twoParamBeta"))

  if (pdf == "ellipsoidal") stopifnot(!missing(chi))
  if (pdf == "twoParamBeta") stopifnot(all(!missing(mu), !missing(nu)))

  switch(
    pdf,
    "planophile" = dplanophile(thetaL),
    "erectophile" = derectophile(thetaL),
    "extremophile" = dextremophile(thetaL),
    "plagiophile" = dplagiophile(thetaL),
    "spherical" = dspherical(thetaL),
    "uniform" = duniform(thetaL),
    "ellipsoidal" = dellipsoidal(thetaL, chi),
    "twoParamBeta" = dtwoParamBeta(thetaL, mu, nu)
  )
}

#' Foliage projection ratio G(θ).
#'
#' @description Compute the mean projection of unit leaf area on the plane
#' perpendicular to beam direction, namely, G(θ) parameter. Assumption of
#' symmetric distribution of leaf azimuth angle.
#' When estimating G for large amount of θ values, it is advised to enable
#' the lookup table for speeding up the calculation.
#' @details Leaf Angle Distribution functions
#' * de Wit’s leaf angle distribution functions:
#'   * \strong{uniform}, proportion of leaf angle is the same at any angle
#'   * \strong{spherical}, relative frequency of leaf angle is the same as for
#'   surface elements of a sphere
#'   * \strong{planophile}, horizontal leaves most frequent
#'   * \strong{erectophile}, vertical leaves most frequent
#'   * \strong{plagiophile}, oblique leaves most frequent
#'   * \strong{extremophile}, oblique leaves least frequent
#' * \strong{ellipsoidal} distribution function, generalization of the spherical
#' distribution over an ellipsoid. Relative frequency of leaf angle is the same
#' as for surface elements of an ellipsoid. Takes one parameter `χ` the ratio
#' horizontal axis over vertical axis. For `χ = 1` the distribution becomes
#' spherical. For `χ < 1`, the ellipsoid is a prolate spheroid (like a
#' rugby ball). For `χ > 1` the ellipsoid is an oblate spheroid (a sphere that
#' bulges at the equator and is somewhat squashed at the poles).
#' * \strong{two parameters Beta} distribution. Most generic approach from Goal
#' and Strebel (1984) to represent large variety of leaf angle distribution. Takes
#' two parameters `μ` and `ν` that control the shape of the Beta
#' distribution.
#' @param theta a numeric vector, θ, the incident beam inclination, in radian,
#' ranging `[0, π/2]`.
#' @param pdf the name of the probability density function of the leaf angle
#' distribution. One of "uniform", "spherical", "planophile", "erectophile",
#' "plagiophile", "extremophile", "ellipsoidal", "twoParamBeta". Refer to
#' section "Leaf Angle Distribution functions" for details.
#' @param chi a float, χ, parameter of the ellipsoidal leaf angle distribution.
#' The ratio the ratio horizontal axis over vertical axis. See section "Leaf
#' Angle Ditribution functions" for details.
#' @param mu a float, μ, parameter controlling the Beta distribution. See section
#' "Leaf Angle Distribution functions" for details.
#' @param nu a float, ν, parameter controlling the Beta distribution. See section
#' "Leaf Angle Distribution functions" for details.
#' @param with.lut a Boolean, whether to estimate G with a lookup table (LUT).
#' By default the lookup table is automatically generated when length of theta
#' vector is greater than 100.
#' @param lut.precision a float, the increment of the θ sequence ranging
#' from 0 to π/2 for computing the lookup table.
#' @references Wang, W. M., Li, Z. L., & Su, H. B. (2007).
#' Comparison of leaf angle distribution functions: effects on extinction
#' coefficient and fraction of sunlit foliage. Agricultural and Forest
#' Meteorology, 143(1), 106-122.
#' @examples
#' # G(θ) == 0.5 for spherical distribution
#' all(computeG(theta = runif(10, 0, pi/2)) == 0.5) # returns TRUE
#' # ellipsoidal distribution
#' computeG(theta = runif(10, 0, pi/2), pdf = "ellipsoidal", chi = 0.6)
#' @seealso [AMAPVox::plotGtheta()]
#' @export
computeG <- function(theta, pdf = "spherical", chi, mu, nu,
                       with.lut = length(theta) > 100, lut.precision = 0.001) {

  stopifnot(all(is.numeric(theta)))
  # normalize theta in [0, pi / 2]
  theta <- theta %% pi
  theta <- ifelse(theta > (pi / 2), pi - theta, theta)

  if (with.lut & lut.precision > 0) {
    theta.lut <- seq(0, pi / 2, by = lut.precision)
    lut <- extinction(theta.lut, pdf, chi, mu, nu,
                      with.lut = FALSE)
    theta.round <- round(theta / lut.precision) * lut.precision
    theta.round[theta.round > max(theta.lut)] <- max(theta.lut)
    return ( lut[sapply(theta.round, function(t) which(theta.lut == t))] )

  } else {
    return ( sapply(theta, computeGtheta, pdf, chi, mu, nu) )
  }
}

# internal function to compute G for a single theta value
computeGtheta <- function(theta, pdf, chi, mu, nu) {

  stopifnot(length(theta) == 1)
  stopifnot(is.character(pdf))

  # special case G(theta) for spherical leaf angle distribution is always 0.5
  # save computation time
  if (pdf == "spherical") return(0.5)

  thetaL.bin <- seq(0, pi / 2, length.out = 181 * 7)
  nbin <- length(thetaL.bin) - 1
  dthetaL <- thetaL.bin[-1] - thetaL.bin[-(nbin + 1)]
  thetaL.x <- 0.5 * (thetaL.bin[-1] + thetaL.bin[-(nbin + 1)])

  # large number of bins allows to skip proper integration of dleaf function
#  fj <- sapply(seq(1, nbin), function(j) integrate(dleaf, thetaL.bin[j], thetaL.bin[j + 1], pdf)$value)
  fj <- dleaf(thetaL.x, pdf, chi, mu, nu) * dthetaL

  # avoid tan(pi/2)
  theta.corr <- ifelse(theta == pi / 2,  pi / 2 - 0.00000001, theta)

  # internal auxiliary function used in computation of extinction coefficient
  A <- function(thetaL) {
    cotcot <-  1 / (tan(theta.corr) * tan(thetaL))
    suppressWarnings(
      return(
        ifelse(
          abs(cotcot) > 1 | is.infinite(cotcot),
          cos(theta.corr) * cos(thetaL),
          cos(theta.corr) * cos(thetaL) * (1 + (2 / pi) * (tan(acos(cotcot)) - acos(cotcot))))
      )
    )
  }

  # large number of bins allows to skip proper integration of A function
#  hj <- sapply(seq(1, nbin), function(j) integrate(A, thetaL.bin[j], thetaL.bin[j + 1])$value)
  hj <- A(thetaL.x) * dthetaL

  # return G(theta)
  sum(fj * hj / dthetaL)
}


#' Plot G(θ) profiles for one or several leaf angle distribution functions
#'
#' @description Plot G(θ) profiles for one or several leaf angle distribution
#' functions with `θ in [0, π/2]`. Requires ggplot2 package.
#' @param pdf the name of the leaf angle distribution functions. One of
#' "uniform", "spherical", "planophile", "erectophile", "plagiophile",
#' "extremophile", "ellipsoidal", "twoParamBeta".
#' @param chi a float, χ, parameter of the ellipsoidal leaf angle distribution.
#' The ratio the ratio horizontal axis over vertical axis. See section "Leaf
#' Angle Ditribution functions" for details.
#' @param mu a float, μ, parameter controlling the Beta distribution. See section
#' "Leaf Angle Distribution functions" for details.
#' @param nu a float, ν, parameter controlling the Beta distribution. See section
#' "Leaf Angle Distribution functions" for details.
#' @examples
#' # plot G(θ) for planophile leaf angle distribution function
#' AMAPVox::plotG(pdf = "planophile")
#' # plot G(θ) for every distributions
#' AMAPVox::plotG()
#' @export
plotG <- function(pdf = c("planophile", "erectophile", "extremophile",
                                   "plagiophile", "spherical", "uniform",
                                   "ellipsoidal", "twoParamBeta"),
                           chi = 0.6, mu = 1.1, nu = 1.3) {

  # check for ggplot2 package
  if (!requireNamespace("ggplot2", quietly = TRUE)) {
    stop(
      "Package \"ggplot2\" must be installed to plot extinction coefficient profiles.",
      "\n",
      "> install.packages(\"ggplot2\")",
      call. = FALSE)
  }

  theta <- seq(0, pi / 2, length.out = 91)
  data <- data.frame()
  for (lad in pdf) {
    LAD <- lad
    LAD <- switch(
      lad,
      "ellipsoidal" = paste0("ellipsoidal (\u03C7=", chi, ")"),
      "twoParamBeta" = paste0("twoParamBeta (\u03BC=", mu, ", \u03BD=", nu, ")"),
      lad
    )
    df <- data.frame(theta = theta * (180 / pi),
                     Gtheta = computeG(theta, pdf = lad, chi, mu, nu),
                     LAD)
    data <- rbind(data, df)
  }

  suppressPackageStartupMessages(require(ggplot2))
  ggplot(data = data, aes(x=theta, y=Gtheta)) +
    geom_line(aes(colour=LAD)) +
    ggtitle(sprintf("Foliage projection ratio G(\u03B8) for given Leaf Angle Distribution (LAD)")) +
    xlab(sprintf("Beam angle \u03B8 [0:90\u00B0]")) +
    ylab(sprintf("G(\u03B8)"))
}
