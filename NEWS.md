# AMAPVox 0.12.0

*2022-08-24*
Added new function "run" that either runs AMAPVox in batch mode or launches GUI
Deleted gui/run check.update option, assuming that version set to "latest" implies to check for updates
Released on CRAN.

# AMAPVox 0.11.0

*2022-07-25*
Function readVoxelSpace can read zipped voxel file.
Function plot accepts voxel subset in y parameter.
Function plantAreaDensity returns separate data table and accepts voxel subset. 
Not released on CRAN.

# AMAPVox 0.10.1

*2022-04-21*
Bugfix in "toRaster" function.
Bugfix across several functions ("canopyHeight", "groundElevation", "plantAreaIndex") for merging/joinging data.table based on "on" instead of "key".
Bugfix in "computeG" function that speed up calculation for large theta vector.
Not released on CRAN.


# AMAPVox 0.10.0

*2022-04-20*
Added new functions "plantAreaDensity", "plantAreaIndex".
Added new function "toRaster".
Released on CRAN.

# AMAPVox 0.9.0

*2022-03-30*
Added new functions "ground", "aboveGround", "belowGround", "groundEnergy", "groundElevation".
Added new functions "canopy", "aboveCanopy", "belowCanopy", "canopyHeight".
Updated "removeButterfly" into "butterfly" function.
Not released on CRAN.

# AMAPVox 0.8.0

*2022-03-24*
Added new function "plantAreaDensity"" to compute plant area density.
Added new function "fillNA" to fill missing values in voxel space.
Added new function "crop" to crop a voxel space to a specified cuboid.
Not released on CRAN.

# AMAPVox 0.7.0

*2022-03-04*
Added new function "removeButterfly" to remove butterfly in voxel space.
Not released on CRAN.

# AMAPVox 0.6.0

*2022-02-23*
Bug fix in writeVoxelSpace function.
Not released on CRAN.

# AMAPVox 0.5.0

*2021-10-19*
Normalized local paths.
RGL library not imported, suggested only.
Not released on CRAN.

# AMAPVox 0.4.0

*2021-10-04*
Bug fix in the version manager. 
Added first draft of vignettes.
Not released on CRAN.

# AMAPVox 0.3.0

*2021-09-03*
Added gui function for running AMAPVox Graphical User Interface.
Not released on CRAN.

# AMAPVox 0.2.0

*2021-07-13*
Added 3d plot for voxel space.
Not released on CRAN.

# AMAPVox 0.1.0

*2021-07-09*
Read/write voxel spaces.
Release on CRAN.
