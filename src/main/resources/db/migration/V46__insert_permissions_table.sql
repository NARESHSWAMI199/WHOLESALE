INSERT IGNORE INTO `permissions` (`permission`, `display_name`, `permission_for`) VALUES

-- Dashboard
('dashboard.count',                'Show Dashboard Statistics',           'Dashboard'),

-- Groups / Roles
('group.all',                      'View All Groups',               'Group'),
('group.permission.all',           'View Group Permissions',              'Group'),
('group.permission.edit',          'Edit Group Permissions',              'Group'),
('group.detail',                   'View Group Details',                  'Group'),
('group.delete',                   'Delete Group',                        'Group'),

-- Items & Related
('item.all',                       'View All Items',                      'Item'),
('item.detail',                    'View Item Details',                   'Item'),
('item.edit',                      'Edit Item',                           'Item'),
('item.import',                    'Import Items',                        'Item'),
('item.export',                    'Export Items',                        'Item'),
('item.delete',                    'Delete Item',                         'Item'),
('item.stock',                     'Manage Item Stock',                   'Item'),
('item.status',                    'Change Item Status',                  'Item'),
('item.image',                     'Manage Item Images',                  'Item'),
('item.category',                  'Manage Item Categories',              'Item'),
('item.category.edit',             'Edit Item Categories',                'Item'),
('item.category.delete',           'Delete Item Category',                'Item Category'),
('item.subcategory',               'Manage Item Subcategories',           'Item Subcategory'),
('item.subcategory.edit',          'Edit Item Subcategory',               'Item Subcategory'),
('item.subcategory.delete',        'Delete Item Subcategory',             'Item Subcategory'),
('item.measuring.unit',            'Manage Measuring Units',              'Measuring Unit'),
('item.report.all',                'View All Item Reports',               'Item Report'),
('item.review.all',                'View All Item Reviews',               'Item Review'),

-- Remove background
('remove.bg.image.upload',         'Remove Background Image Upload',       'Item'),
('remove.bg.image.download',       'Remove Background Image Download',     'Item'),

-- Pagination (usually internal but still can be permission controlled)
('pagination.all',                 'Manage Pagination Settings',          'Pagination'),
('pagination.edit',                 'Update pagination Setting',          'Pagination'),

-- User Plans & Service Plans
('user.plan.detail',               'View User Plan Details',              'User Plan'),
('user.plan.all',                  'View All User Plans',                 'User Plan'),
('service-plans.all',              'View All Service Plans',              'Service Plan'),
('service-plans.add',              'Create New Service Plan',             'Service Plan'),
('service-plans.status.update',    'Change Service Plan Status',          'Service Plan'),
('service-plans.delete',           'Delete Service Plan',                 'Service Plan'),

-- Stores
('store.all',                      'View All Stores',                     'Store'),
('store.delete',                   'Delete Store',                        'Store'),
('store.detail',                   'View Store Details',                  'Store'),
('store.user',                     'Manage Store Users',                  'Store'),
('store.edit',                     'Edit Store',                          'Store'),
('store.profile.edit',             'Edit Store Profile',                  'Store'),
('store.status',                   'Change Store Status',                 'Store'),
('store.profile',                  'View Store Profile',                  'Store'),
('store.category.edit',            'Edit Store Category',                 'Store Category'),
('store.category.detail',          'View Store Category Details',         'Store Category'),
('store.category.delete',          'Delete Store Category',               'Store Category'),
('store.subcategory.all',          'View All Store Subcategories',        'Store Subcategory'),
('store.subcategory.delete',       'Delete Store Subcategory',            'Store Subcategory'),
('store.subcategory.edit',         'Edit Store Subcategory',              'Store Subcategory'),
('store.report.all',               'View All Store Reports',              'Store Report'),

-- Wallet
('wallet.transactions',            'View Wallet Transactions',            'Wallet'),
('wallet.detail',                  'View Wallet Details',                 'Wallet'),
('wallet.pay',                     'Make Payment from Wallet',            'Wallet'),

-- Users
('user.all',                       'View All Users',                      'User'),
('user.edit',                      'Edit User',                           'User'),
('user.detail',                    'View User Details',                   'User'),
('user.delete',                    'Delete User',                         'User'),
('user.reset.password',            'Reset User Password',                 'User'),
('user.status',                    'Change User Status',                  'User'),
('user.profile.edit',              'Edit User Profile',                   'User Profile'),
('user.profile',                   'View User Profile',                   'User Profile'),
('user.groups',                    'Manage User Groups',                  'User'),

-- Wholesaler
('wholesaler.permission',          'View Wholesaler Permissions',         'Wholesaler'),
('wholesaler.permission.update',   'Update Wholesaler Permissions',       'Wholesaler');