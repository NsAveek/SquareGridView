package com.example.customgridview.ui.permission

interface IPermission {
    fun checkPermission() : Boolean
    fun requestPermission()

}