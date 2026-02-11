export enum Role {
  GUEST = 'GUEST',
  BASIC_USER = 'BASIC_USER',
  ADMIN_USER = 'ADMIN_USER',
  SUPER_USER = 'SUPER_USER'
}

export const hasRole = (userRole: string | undefined, requiredRole: Role): boolean => {
  if (!userRole) return false;
  
  const roleHierarchy: Record<string, number> = {
    GUEST: 0,
    BASIC_USER: 1,
    ADMIN_USER: 2,
    SUPER_USER: 3
  };
  
  return (roleHierarchy[userRole] || 0) >= roleHierarchy[requiredRole];
};

export const isAdmin = (userRole: string | undefined): boolean => {
  return hasRole(userRole, Role.ADMIN_USER);
};

export const isSuperUser = (userRole: string | undefined): boolean => {
  return hasRole(userRole, Role.SUPER_USER);
};

export const isBasicUser = (userRole: string | undefined): boolean => {
  return hasRole(userRole, Role.BASIC_USER);
};

export const isOwner = (userId: string | undefined, resourceOwnerId: string | undefined): boolean => {
  if (!userId || !resourceOwnerId) return false;
  return userId === resourceOwnerId;
};

export const canPerformAction = (
  userId: string | undefined,
  userRole: string | undefined,
  resourceOwnerId: string | undefined,
  requiredRole: Role = Role.ADMIN_USER
): boolean => {
  return isOwner(userId, resourceOwnerId) || hasRole(userRole, requiredRole);
};