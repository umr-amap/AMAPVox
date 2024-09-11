# AMAPVox 2.3.1

*yyyy-MM-dd*

Released/Not release on CRAN

# AMAPVox 2.3.0

*2024-09-09*
New feature: trajectory time span [#41](https://forge.ird.fr/amap/amapvox/AMAPVox/-/issues/41)
New feature: optional normalization for relative echo weight [#50](https://forge.ird.fr/amap/amapvox/AMAPVox/-/issues/50)
New feature: user-defined Plant Area Density variable and Leaf Angle Distribution in canopy tools [#40](https://forge.ird.fr/amap/amapvox/AMAPVox/-/issues/40), [#49](https://forge.ird.fr/amap/amapvox/AMAPVox/-/issues/49)
New feature: AMAPVox::plot3d axis can either be i, j, k or x, y, z [#47](https://forge.ird.fr/amap/amapvox/AMAPVox/-/issues/47)
Bugfix: removed differential reflectance between ground and vegation [#38](https://forge.ird.fr/amap/amapvox/AMAPVox/-/issues/38)
Bugfix: LAS points outside trajectory time span automatically discarded [#45](https://forge.ird.fr/amap/amapvox/AMAPVox/-/issues/45)
Bugfix: SOP, POP & VOP transformation matrix were always enabled [#42](https://forge.ird.fr/amap/amapvox/AMAPVox/-/issues/42)
Bugfix: erroneous Zmax value in voxel boundary quick search [#44](https://forge.ird.fr/amap/amapvox/AMAPVox/-/issues/44)

Not released on CRAN

# AMAPVox 2.2.1

*2024-03-15*
Aligned R package version onto Java core version
Updated binary packages URL [#31](https://forge.ird.fr/amap/amapvox/AMAPVox/-/issues/31), [#32](https://forge.ird.fr/amap/amapvox/AMAPVox/-/issues/32)
Bugfix in transmittance map tool [#37](https://forge.ird.fr/amap/amapvox/AMAPVox/-/issues/37)
Bugfix in XYB point cloud reader [#30](https://forge.ird.fr/amap/amapvox/AMAPVox/-/issues/30)

Released on CRAN

# AMAPVox 1.0.1

*2023-06-19*
Fixed issue [#5](https://forge.ird.fr/amap/amapvox/AMAPVox/-/issues/5), unzip failed on Windows and AMAPVox did not run.
Deleted Java as System Requirement in DESCRIPTION (optional since AMAPVox binary 2.0)

Released on CRAN

# AMAPVox 1.0.0

*2023-03-15*
From binary AMAPVox 2.0, no need to install local Java VM, it is embedded within AMAPVox.
Released on CRAN

# AMAPVox 0.13.0

*2023-01-26*
Get AMAPVox binaries from forge.ird.fr with Gitlab API
Not released on CRAN.

# AMAPVox 0.12.1

*2022-12-05*
Fixed issue [#2](https://forge.ird.fr/amap/amapvox/AMAPVox/-/issues/2) with data.table.merge
Released on CRAN.

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
