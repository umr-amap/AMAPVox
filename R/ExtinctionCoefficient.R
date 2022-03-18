
#
# leaf angles in radians
# leaf angle = 0 for horizontal leaves
# leaf angle = pi / 2 for vertical leaves
#
# no preferred azimutal direction
#

# probability density of leaf angle distribution
# mostly horizontal leaves
planophile <- function(thetaL) {
  stopifnot(dplyr::between(thetaL, 0, pi / 2))
  # probability density of eaf angle distribution
  (2 / pi) * (1 + cos(2 * thetaL))
}

erectophile <- function(thetaL) {
  stopifnot(dplyr::between(thetaL, 0, pi / 2))
  (2 / pi) * (1 - cos(2 * thetaL))
}

plagiophile <- function(thetaL) {
  stopifnot(dplyr::between(thetaL, 0, pi / 2))
  (2 / pi) * (1 - cos(4 * thetaL))
}

extremophile <- function(thetaL) {
  stopifnot(dplyr::between(thetaL, 0, pi / 2))
  (2 / pi) * (1 + cos(4 * thetaL))
}

spherical <- function(thetaL) {
  stopifnot(dplyr::between(thetaL, 0, pi / 2))
  sin(thetaL)
}

uniform <- function(thetaL) {
  stopifnot(dplyr::between(thetaL, 0, pi / 2))
  2 / pi
}

ellipsoidal <- function(thetaL, chi) {
  stopifnot(dplyr::between(thetaL, 0, pi / 2))
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

twoParamBeta <- function(thetaL, mu, nu) {
  stopifnot(dplyr::between(thetaL, 0, pi / 2))
  stopifnot(all(mu >= 0, nu >= 0))
  t <- 2 * thetaL / pi
  dbeta(t, mu, nu) * (2 / pi)
}

# probability density function of leaf angle distribution
# thetaL the leaf inclination angle in radian
# pdf the name of the probability density function
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
    "planophile" = planophile(thetaL),
    "erectophile" = erectophile(thetaL),
    "extremophile" = extremophile(thetaL),
    "plagiophile" = plagiophile(thetaL),
    "spherical" = spherical(thetaL),
    "uniform" = uniform(thetaL),
    "ellipsoidal" = ellipsoidal(thetaL, chi),
    "twoParamBeta" = twoParamBeta(thetaL, mu, nu)
  )
}



extinction <- function(theta, pdf = "spherical", chi, mu, nu) {

  stopifnot(all(is.numeric(theta)))
  # normalize theta in [0, pi / 2]
  theta <- theta %% pi
  theta <- ifelse(theta > (pi / 2), pi - theta, theta)

  sapply(theta, .computeG, pdf, chi, mu, nu)
}


.computeG <- function(theta, pdf, chi, mu, nu) {

  stopifnot(length(theta) == 1)
  stopifnot(is.character(pdf))

  # special case G(theta) for spherical leaf angle distribution is always 0.5
  # save computation time
  if (pdf == "spherical") return(0.5)

  thetaL.bin <- seq(0, pi / 2, length.out = 181 * 7)
  nbin <- length(thetaL.bin) - 1
#  thetaL.bin[1] <- 0.00000001
#  thetaL.bin[lnbin + 1] <- pi / 2 - 0.00000001
  dthetaL <- thetaL.bin[-1] - thetaL.bin[-(nbin + 1)]
  thetaL.x <- 0.5 * (thetaL.bin[-1] + thetaL.bin[-(nbin + 1)])

  # large number of bins allows to skip proper integration of dleaf function
#  fj <- sapply(seq(1, nbin), function(j) integrate(dleaf, thetaL.bin[j], thetaL.bin[j + 1], pdf)$value)
  fj <- dleaf(thetaL.x, pdf, chi, mu, nu) * dthetaL

  # internal auxiliary function used in computation of extinction coefficient
  A <- function(thetaL) {
    if (theta == pi / 2)
      return(0)
    else {
      cotcot <-  1 / (tan(theta) * tan(thetaL))
      suppressWarnings(
        return(
          ifelse(
            abs(cotcot) > 1 | is.infinite(cotcot),
            cos(theta) * cos(thetaL),
            cos(theta) * cos(thetaL) * (1 + (2 / pi) * (tan(acos(cotcot)) - acos(cotcot))))
        )
      )
    }
  }

  # large number of bins allows to skip proper integration of A function
#  hj <- sapply(seq(1, nbin), function(j) integrate(A, thetaL.bin[j], thetaL.bin[j + 1])$value)
  hj <- A(thetaL.x) * dthetaL

  # return G(theta)
  sum(fj * hj / dthetaL)
}

#
#plot(seq(0, pi/2, length.out = 120) / pi, extinction(seq(0, pi/2, length.out = 120), "ellipsoidal", 0.1))
