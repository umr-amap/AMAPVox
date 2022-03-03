
removeButterfly <- function(vxsp, f.out = vxsp@file) {

  # must be a voxel space
  stopifnot(is.VoxelSpace(vxsp))

  # pointer to voxels
  vx <- vxsp@voxels

  # subset of voxels with nbEchos > 0
  vx.hit.index <- vx[nbEchos > 0, which = TRUE]
  vx.hit <- vx[vx.hit.index, .(i, j, k, nbEchos)]

  # moore neighborhood of rank 1
  neighbors <- RANN::nn2(data = vx.hit[, .(i, j, k)],
                         k = 27, searchtype = "radius", radius = 1.8)
  # remove current voxel from neighbors
  neighbors <- neighbors$nn.idx[, -1]

  # identify butterflies
  # butterflies = voxels without neighbors in subset of voxels with nbEchos > 0
  butterflies <- which(
    apply(neighbors, 1, function(neighbor) all(neighbor == 0)))

  if (length(butterflies) == 0) {
    print("No butterfly found. Nothing to do.")
  } else {
    # clean butterflies
    vx[vx.hit[butterflies], `:=`(nbEchos = 0,
                                 bsIntercpted = 0,
                                 PadBVTotal = 0,
                                 transmittance = 1,
                                 attenuation_FPL_biasedMLE = 0,
                                 attenuation_FPL_biasCorrection = 0,
                                 attenuation_FPL_unbiasedMLE = 0,
                                 attenuation_PPL_MLE = 0),
       on = .(i, j, k)]

    cat("Removed", length(butterflies), "butterflies in",
        basename(vxsp@file), "\n")

    # only keep original columns
    vxsp@voxels <- vx[, .SD, .SDcols = getParameter(vxsp, "columnNames")]

    # write butterfly-free voxel file
    AMAPVox::writeVoxelSpace(vxsp, f.out)
  }
}
