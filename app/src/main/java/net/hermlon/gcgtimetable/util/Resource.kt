package net.hermlon.gcgtimetable.util

enum class ResourceStatus { SUCCESS, LOADING, ERROR, ERROR_OFFLINE, ERROR_AUTH, ERROR_NOT_FOUND }

class Resource<T>(var status: ResourceStatus, var data: T? = null)