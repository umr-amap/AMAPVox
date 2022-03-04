
#' Plot an object of class VoxelSpace
#'
#' Plot an object of class VoxelSpace in a 3d device. By default it plots the
#' sampling intensity but the user can choose any variable available in the
#' voxel file.
#'
#' @description plot a \code{\link{VoxelSpace-class}} object.
#' @param x the object of class VoxelSpace to plot
#' @param y Unused (inherited from R base)
#' @param variable.name character, the name of the variable to plot
#' @param palette character, a valid palette name (one of hcl.pals())
#' @param bg.color character, a valid background color name (one of colors())
#' @param width numeric, the width of the windows
#' @param voxel.size numeric, the size of voxel in pixels
#' @param unsampled.discard logical, whether to discard unsampled voxel
#' @param empty.discard logical, whether to discard empty voxel (no hit)
#' @param ... additional parameters which will be passed to
#'   \code{\link[rgl]{plot3d}}.
#' @include Classes.R
#' @seealso \code{\link[rgl]{plot3d}}
#' @examples
#' \dontrun{
#' # load a voxel file
#' vxsp <- readVoxelSpace(system.file("extdata", "tls_sample.vox", package = "AMAPVox"))
#' # plot sampling intensity by default
#' plot(vxsp)
#' # plot PAD
#' plot(vxsp, variable.name = "PadBVTotal", palette = "YlOrRd")
#' }
#' @export
#' @method plot VoxelSpace
setGeneric("plot", function(x, y, ...)
standardGeneric("plot"))

#' @rdname plot
setMethod("plot",
          signature(x = "VoxelSpace", y = "missing"),
          function(x, y, variable.name = "nbSampling",
                           palette = "viridis", bg.color = "lightgrey",
                           width = 640, voxel.size = 5,
                           unsampled.discard = TRUE, empty.discard = TRUE,
                           ...) {

 # check if rgl package is installed
 if (!requireNamespace("rgl", quietly = TRUE)) {
   stop(
     "Package \"rgl\" must be installed to plot VoxelSpace object",
     "\n",
     ">> install.packages(\"rgl\")",
      call. = FALSE)
  }
  # must be a voxel space
  stopifnot(is.VoxelSpace(x))
  # y not used
  stopifnot(missing(y))
  # make sure variable exists
  stopifnot(variable.name %in% colnames(x@voxels))
  # make sure variable nbSampling exists if discard unsampled voxel is TRUE
  stopifnot(empty.discard | ('nbSampling' %in% colnames(x@voxels)))
  # make sure variable nbEchos exists if discard empty voxel is TRUE
  stopifnot(empty.discard | ('nbEchos' %in% colnames(x@voxels)))

  # discard empty voxels
  vx <- x@voxels
  nbSampling <- nbEchos <- NULL # due to NSE notes in R CMD check
  if (unsampled.discard) vx <- vx[nbSampling > 0]
  if (empty.discard) vx <- vx[nbEchos > 0]
  # compute x, y, z positions
  pos <- getPosition(x, vx)
  # extract variable to plot
  variable <- unlist(vx[, variable.name, with = FALSE])
  # variable range
  varLim <- range(variable)
  varLen <- varLim[2] - varLim[1]
  # color look-up table
  colorlut <- grDevices::hcl.colors(1024, palette = palette)
  col <- colorlut[ceiling(1023 * (variable - varLim[1]) / varLen) + 1]
  # palette
  palette(colorlut)
  # 3d plot
  rgl::par3d(windowRect = 200 + c( 0, 0, width, 0.8 * width ) )
  rgl::plot3d(pos$x, pos$y, pos$z,
         col=col, size=voxel.size, aspect="iso",
         xlab="x", ylab="y", zlab="z")
  rgl::bgplot3d({
    # background color
    graphics::par(bg= bg.color)
    graphics::plot.new()
    # colorbar
    fields::image.plot(legend.only = TRUE, add = TRUE, zlim = varLim,
                       col = colorlut)
    # main title
    graphics::title(main = paste('Voxel space - Variable', variable.name))
  })
})

