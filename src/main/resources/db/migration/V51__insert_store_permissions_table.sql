INSERT IGNORE INTO store_permissions (permission, display_name, default_permission, permission_for) VALUES

-- IMAGE / TOOLS
('remove.bg.image.upload', 'Remove Background Image Upload', 'Y', 'Item'),
('remove.bg.image.download', 'Remove Background Image Download', 'Y', 'Item'),

-- WALLET
('wallet.transactiona.all', 'Wallet Transactions', 'Y', 'Wallet'),
('wallet.dashboard.graph', 'Wallet Dashboard Graph', 'Y', 'Wallet'),
('wholesale.wallet.pay', 'Wallet Payment', 'Y', 'Wallet'),

-- DASHBOARD
('wholesale.dashboard.count', 'Dashboard Summary Count', 'Y', 'Dashboard'),

-- PLANS
('wholesale.furture.plans.all', 'Future Plans', 'Y', 'Plan'),
('wholesale.furture.plans.activate', 'Activate Future Plan', 'Y', 'Plan'),
('wholesale.plan.all', 'Plan List', 'Y', 'Plan'),
('wholesale.plan.detail', 'Plan Details', 'Y', 'Plan'),
('wholesale.plan.active', 'Activate Plan', 'Y', 'Plan'),
('wholesale.my.current.plan', 'My Current Plan', 'Y', 'Plan'),

-- EXCEL
('excel.notUpdated.absolute', 'Excel Not Updated', 'Y', 'Item'),

-- ITEMS
('wholesale.item.template.download', 'Download Item Template', 'Y', 'Item'),
('wholesale.item.not.updated.download', 'Download Not Updated Items', 'Y', 'Item'),
('wholesale.item.export', 'Export Items', 'Y', 'Item'),
('wholesale.item.import', 'Import Items', 'Y', 'Item'),
('wholesale.item.stock.update', 'Update Item Stock', 'Y', 'Item'),
('wholesale.item.delete', 'Delete Item', 'Y', 'Item'),
('wholesale.item.edit', 'Edit Item', 'Y', 'Item'),
('wholesale.item.detail', 'View Item Details', 'Y', 'Item'),
('wholesale.item.all', 'View All Items', 'Y', 'Item'),

-- REVIEWS
('wholesale.review.all', 'View Reviews', 'Y', 'Item'),

-- PAGINATION / SETTINGS
('wholesale.pagination.all', 'View Pagination Settings', 'Y', 'Settings'),
('wholesale.pagination.edit', 'Edit Pagination Settings', 'Y', 'Settings'),

-- PROMOTIONS
('wholesale.promoted.item.add', 'Add Promoted Item', 'Y', 'Item'),

-- STORE
('wholesale.store.create', 'Create Store', 'Y', 'Store'),
('wholesale.store.edit', 'Edit Store', 'Y', 'Store'),
('wholesale.store.notifications', 'View Store Notifications', 'Y', 'Store'),
('wholesale.store.notifications.seen', 'Mark Notifications As Seen', 'Y', 'Store'),

-- USER / PROFILE
('wholesale.profile.edit', 'Edit Profile', 'Y', 'User'),
('wholesale.password.reset', 'Reset Password', 'Y', 'User');

