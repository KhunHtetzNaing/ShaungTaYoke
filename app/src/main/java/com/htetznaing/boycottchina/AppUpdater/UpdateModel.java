package com.htetznaing.boycottchina.AppUpdater;

public class UpdateModel {
    String versionName,title,message,download,playstore,hash;
    int versionCode;
    boolean uninstall,all=true,force=true,app_gallery = false;
    String[] models;
    int []versions;

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public boolean isApp_gallery() {
        return app_gallery;
    }

    public void setApp_gallery(boolean app_gallery) {
        this.app_gallery = app_gallery;
    }

    public boolean isForce() {
        return force;
    }

    public void setForce(boolean force) {
        this.force = force;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public int getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(int versionCode) {
        this.versionCode = versionCode;
    }

    public String getPlaystore() {
        return playstore;
    }

    public void setPlaystore(String playstore) {
        this.playstore = playstore;
    }

    public boolean isUninstall() {
        return uninstall;
    }

    public void setUninstall(boolean uninstall) {
        this.uninstall = uninstall;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDownload() {
        return download;
    }

    public void setDownload(String download) {
        this.download = download;
    }


    public boolean isAll() {
        return all;
    }

    public void setAll(boolean all) {
        this.all = all;
    }

    public String[] getModels() {
        return models;
    }

    public void setModels(String[] models) {
        this.models = models;
    }

    public int[] getVersions() {
        return versions;
    }

    public void setVersions(int []versions) {
        this.versions = versions;
    }
}