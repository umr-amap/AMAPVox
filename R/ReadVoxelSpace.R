
## given a string "numeric separator numeric separator numeric ..." this
## function will test for several separators for splitting and returns the one
## with highest occurrence.
.guessSeparator = function(str){
  SEPARATORS = c(space = " " , equal = "=", semicolon = ";",
                 coma = ",", colon = ":", tab = "\t")
  guess = which.min(nchar(lapply(stringr::str_split(str,SEPARATORS), "[", i = 1)))
  separator = SEPARATORS[guess]

  return(separator)
}

## 3d point coordinates in voxel file header may be written in several ways.
## this function parses the string and returns a (x, y, z) vector.
## Accepted formats: {[( numeric separator numeric separator numeric ... )]}
##     leading and trailing brackets are removed
##     guess separator between numeric values and split
.parseNumericVector = function(str) {
  vec <- as.numeric(unlist(stringr::str_split(stringr::str_squish(stringr::str_remove_all(str, "[\\(\\)\\[\\]\\{\\}]")), .guessSeparator(str))))
  if (length(vec) == 1) vec <- rep(vec, 3)
  names(vec) <- c("x", "y", "z")
  return(vec)
}

#' Read a voxel file
#'
#' @docType methods
#' @rdname readVoxelSpace
#' @description read a voxel file and cast it into a \code{\link{VoxelSpace-class}} object.
#' @param f The path of the voxel file.
#' @include AMAPVoxClasses.R
#' @seealso \code{\link{writeVoxelSpace}}
#' @export
readVoxelSpace <- function(f){

  vox=new(Class=("VoxelSpace"))

  #lecture du header
  conn <- file(f, open="r")

  # check 1st line VOXEL FILE
  firstLine <- readLines(conn, n=1)
  stopifnot(!is.na(stringr::str_match(stringr::str_trim(firstLine), "^VOXEL SPACE$")))

  # set file slot
  vox@file <- f

  # loop over header
  parameters <- NULL
  nLineHeader = 0
  while ( TRUE ) {
    # read next line
    line = stringr::str_squish(readLines(conn, n = 1))
    nLineHeader = nLineHeader + 1;
    # check if line starts with hash
    if ( stringr::str_starts(line, "#") ) {
      # parse header line
      # may be several parameters on same line, separated by hash
      lineSplit <- stringr::str_squish(stringr::str_split(line, "#")[[1]][-1])
      # split key:value
      lineParam <- sapply(lineSplit, function(p) unlist(stringr::str_split(p, ":")))
      colnames(lineParam) <- as.character(lineParam[1,])
      parameters <- c(parameters, lineParam[-1,])
    } else {
      break
    }
  }

  # closes vox file
  close(conn)

  ## Predefined parameters
  # number of lines
  vox@header@nline <- as.integer(nLineHeader)
  # column names
  columnNames = unlist(stringr::str_split(line, " "))
  vox@header@columnNames <- columnNames
  # min corner
  vox@header@mincorner <- .parseNumericVector(parameters["min_corner"])
  # max corner
  vox@header@maxcorner <- .parseNumericVector(parameters["max_corner"])
  # split
  vox@header@split <- .parseNumericVector(parameters["split"])
  # resolution
  vox@header@resolution <- .parseNumericVector(parameters["res"])
  ## All parameters as characters
  vox@header@parameters <- parameters

  #lecture des voxels
  #instance@voxels= read.table(file, header=T,skip=5)
  vox@voxels = data.table::fread(f, header = T, skip = nLineHeader)

  return (vox)
}

## specific implementation of the "show" function for a VoxelSpace object
showVoxelSpace <- function(voxelSpace) {

  cat(class(voxelSpace)[1],'\n')
  cat("  file",  voxelSpace@file, sep='\t', '\n')
  writeLines(paste0("  ", paste(names(voxelSpace@header@parameters), voxelSpace@header@parameters, sep='\t')))
  cat("  output variables", paste(voxelSpace@header@columnNames, collapse=", "), '\n', sep='\t')
  show(voxelSpace@voxels)
}
