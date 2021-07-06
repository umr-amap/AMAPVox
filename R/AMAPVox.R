#' AMAPVox package
#'
#' The package provides a a set of R functions for reading, manipulating
#' and writing voxel spaces. Voxel spaces are read from text-based output files
#' of the AMAPVox software.
#'
#' As of version 0.1, available functions are limited and rudimentary, basically
#' read/write voxel space. With time the package will include functions to
#' launch full GUI tool from R and a set of useful pre/post-processing functions.
#'
#' @section References:
#' \itemize{
#'   \item{
#'   Research paper first describing AMAPVox:\cr
#'     Vincent, G., Antin, C., Laurans, M., Heurtebize, J., Durrieu, S.,
#'     Lavalley, C., & Dauzat, J. (2017). Mapping plant area index of tropical
#'     evergreen forest by airborne laser scanning. A cross-validation study
#'     using LAI2200 optical sensor. Remote Sensing of Environment, 198, 254-266.
#'     \url{https://doi.org/10.1016/j.rse.2017.05.034}
#'   }
#'   \item{
#'   Up-to-date description of PAD/LAD estimators implemented in AMAPVox:\cr
#'     VINCENT, Gregoire; PIMONT, François; VERLEY, Philippe, 2021,
#'     "A note on PAD/LAD estimators implemented in AMAPVox 1.7",
#'     \url{https://doi.org/10.23708/1AJNMP}, DataSuds, V1
#'   }
#' }
#'
#' @section Contact:
#'     \email{contact@amapvox.org}
#'
#' @docType package
#'
#' @author Philippe VERLEY \email{philippe.verley@ird.fr}
#'
#' @name AMAPVox
#'
#' @importFrom methods callGeneric is new show
#' @importFrom utils write.table
#' @importFrom data.table fread
#' @importFrom stringr str_match str_trim str_squish str_starts str_split
#'   str_remove_all
"_PACKAGE"
