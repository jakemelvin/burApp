package com.packt.blurApp.model.enums;

public enum PermissionType {
    // User Management (GREAT_ADMIN only)
    CREATE_USER,
    UPDATE_USER,
    DELETE_USER,
    VIEW_ALL_USERS,
    ASSIGN_ROLES,
    
    // Party Management
    CREATE_PARTY,
    JOIN_PARTY,
    MANAGE_PARTY,
    DELETE_PARTY,
    VIEW_PARTY,
    
    // Race Management
    CREATE_RACE,
    START_RACE,
    JOIN_RACE,
    LEAVE_RACE,
    VIEW_RACE,
    DELETE_RACE,
    
    // Score Management
    SUBMIT_SCORE,
    VIEW_SCORE,
    EDIT_SCORE,
    
    // Car & Map Management
    VIEW_CARS,
    VIEW_MAPS,
    
    // Statistics
    VIEW_STATISTICS,
    VIEW_HISTORY,
    
    // Profile Management
    UPDATE_OWN_PROFILE,
    VIEW_OWN_PROFILE,
    
    // All permissions (GREAT_ADMIN)
    ALL_PERMISSIONS
}
