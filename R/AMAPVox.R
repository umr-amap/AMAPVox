#' AMAPVox package
#'
#' The package provides a a set of R functions for working with voxel spaces
#' (read, write, plot, etc.).
#' Voxel spaces are read from text-based output files of the
#' \href{https://amap-dev.cirad.fr/projects/amapvox}{AMAPVox software}.
#'
#' @section References: \itemize{ \item{ Research paper first describing
#'   AMAPVox:\cr Vincent, G., Antin, C., Laurans, M., Heurtebize, J., Durrieu,
#'   S., Lavalley, C., & Dauzat, J. (2017). Mapping plant area index of tropical
#'   evergreen forest by airborne laser scanning. A cross-validation study using
#'   LAI2200 optical sensor. Remote Sensing of Environment, 198, 254-266.
#'   \doi{10.1016/j.rse.2017.05.034} } \item{ Up-to-date description of PAD/LAD
#'   estimators implemented in AMAPVox:\cr VINCENT, Gregoire; PIMONT, François;
#'   VERLEY, Philippe, 2021, "A note on PAD/LAD estimators implemented in
#'   AMAPVox 1.7", \doi{10.23708/1AJNMP}, DataSuds, V1 } }
#'
#' @section Contact: \email{contact@amapvox.org}
#'
#' @docType package
#'
#' @author Philippe VERLEY \email{philippe.verley@ird.fr}
#'
#' @name AMAPVox
#'
#' @importFrom methods callGeneric is new show
#' @importFrom utils write.table
#' @importFrom data.table data.table fread := .SD
#' @importFrom stringr str_match str_trim str_squish str_starts str_split
#'   str_remove_all str_extract
#' @importFrom dplyr %>%
#' @importFrom rvest read_html html_element html_nodes html_attr
#' @importFrom rappdirs user_data_dir
#' @importFrom curl nslookup
#' @importFrom stats dbeta
# commented since these packages are suggested instead of requested
# @importFrom rgl par3d plot3d bgplot3d
# @importFrom ggplot2 ggplot aes ggtitle geom_line xlab ylab
# @importFrom RANN nn2
"_PACKAGE"
