
## given a string "numeric separator numeric separator numeric ..." this
## function will test for several separators for splitting and returns the one
## with highest occurrence.
.guessSeparator <- function(str){
  SEPARATORS <- c(space = " " , equal = "=", semicolon = ";",
                 coma = ",", colon = ":", tab = "\t")
  guess <- which.min(
    nchar(lapply(stringr::str_split(str,SEPARATORS), "[", i = 1)))
  separator <- SEPARATORS[guess]

  return(separator)
}

## 3d point coordinates in voxel file header may be written in several ways.
## this function parses the string and returns a (x, y, z) vector.
## Accepted formats: {[( numeric separator numeric separator numeric ... )]}
##     leading and trailing brackets are removed
##     guess separator between numeric values and split
.parseNumericVector <- function(str) {
  vec <- as.numeric(unlist(
    stringr::str_split(
      stringr::str_squish(stringr::str_remove_all(str, "[\\(\\)\\[\\]\\{\\}]")),
      .guessSeparator(str))))
  if (length(vec) == 1) vec <- rep(vec, 3)
  names(vec) <- c("x", "y", "z")
  return(vec)
}

#' Read a voxel file
#'
#' @docType methods
#' @rdname readVoxelSpace
#' @description read a voxel file and cast it into a
#'   \code{\link{VoxelSpace-class}} object.
#'
#'   Zipped voxel file is accepted. AMAPVox uses user cache directory to unzip
#'   the file (\link[rappdirs]{user_cache_dir}).
#' @param f The path of the voxel file.
#' @include Classes.R
#' @seealso \code{\link{writeVoxelSpace}}
#' @examples
#' # load a voxel file
#' vxsp <- readVoxelSpace(system.file("extdata", "tls_sample.vox", package = "AMAPVox"))
#' @export
readVoxelSpace <- function(f){

  if (grepl("*.zip$", f)) {
    # unzip first
    cache.dir <- rappdirs::user_cache_dir("AMAPVox", "IRD")
    cat("Unzip voxel file", basename(f), "in cache directory ", cache.dir, "\n")
    f.unzip <- utils::unzip(f,
                            junkpaths = TRUE,
                            exdir = cache.dir)
    vx.file <- normalizePath(f.unzip[1])
  } else {
    vx.file <- normalizePath(f)
  }

  #lecture du header
  conn <- file(vx.file, open="r")

  # check 1st line VOXEL FILE
  firstLine <- readLines(conn, n=1)
  stopifnot(
    !is.na(stringr::str_match(stringr::str_trim(firstLine), "^VOXEL SPACE$")))

  # loop over header
  rawParameters <- list()
  nLineHeader <- 0
  while ( TRUE ) {
    # read next line
    line <- stringr::str_squish(readLines(conn, n = 1))
    nLineHeader <- nLineHeader + 1
    # check if line starts with hash
    if ( stringr::str_starts(line, "#") ) {
      # parse header line
      # may be several parameters on same line, separated by hash
      lineSplit <- stringr::str_squish(stringr::str_split(line, "#")[[1]][-1])
      # split key:value
      lineParam <- vapply(lineSplit,
                          function(p) unlist(stringr::str_split(p, ":")),
                          character(2))
      colnames(lineParam) <- as.character(lineParam[1,])
      rawParameters <- c(rawParameters, lineParam[-1,])
    } else {
      break
    }
  }

  # closes vox file
  close(conn)

  # new VoxelSpace object
  vxsp <- new(Class=("VoxelSpace"))

  # set file slot
  vxsp@file <- f

  ## Predefined parameters
  parameters <- list(
    # number of lines
    nline = as.integer(nLineHeader),
    # column names
    columnNames = unlist(stringr::str_split(line, " ")),
    # min corner
    mincorner = .parseNumericVector(rawParameters["min_corner"]),
    # max corner
    maxcorner = .parseNumericVector(rawParameters["max_corner"]),
    # split
    dim = .parseNumericVector(rawParameters["split"]),
    # voxel size
    voxel.size = .parseNumericVector(rawParameters["res"])
  )
  # Other parameters
  parameters <- c(parameters,
                  rawParameters[!(names(rawParameters) %in%
                                    c("min_corner", "max_corner",
                                      "split", "res")) ])

  vxsp@header <- parameters

  # read voxels
  vxsp@data <- data.table::fread(vx.file, header = TRUE, skip = nLineHeader)

  return (vxsp)
}

## specific implementation of the "show" function for a VoxelSpace object
showVoxelSpace <- function(vxsp) {

  cat(class(vxsp)[1],'\n')
  cat("  file",  vxsp@file, sep='\t', '\n')
  writeLines(paste0("  ",
                    paste(names(vxsp@header),
                          vxsp@header,
                          sep='\t')))
  cat("  output variables",
      paste(vxsp@header$columnNames, collapse=", "),
      '\n',
      sep='\t')
  show(vxsp@data)
}
