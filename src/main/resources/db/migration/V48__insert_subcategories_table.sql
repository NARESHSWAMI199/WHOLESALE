INSERT
IGNORE INTO store_subcategories
(slug, category_id, subcategory, icon, updated_at, is_deleted)
VALUES
-- Kirana & General Store
('daily-essentials', 1, 'Daily Essentials', 'essentials.png', UNIX_TIMESTAMP()*1000, 'N'),
('packed-foods', 1, 'Packed Foods', 'packed.png', UNIX_TIMESTAMP()*1000, 'N'),
('pooja-items', 1, 'Pooja Items', 'pooja.png', UNIX_TIMESTAMP()*1000, 'N'),
('stationery', 1, 'Stationery', 'stationery.png', UNIX_TIMESTAMP()*1000, 'N'),
('baby-care', 1, 'Baby Care', 'baby.png', UNIX_TIMESTAMP()*1000, 'N'),

-- Fruits & Vegetables
('fresh-fruits', 2, 'Fresh Fruits', 'fruits.png', UNIX_TIMESTAMP()*1000, 'N'),
('fresh-vegetables', 2, 'Fresh Vegetables', 'vegetables.png', UNIX_TIMESTAMP()*1000, 'N'),
('leafy-vegetables', 2, 'Leafy Vegetables', 'leafy.png', UNIX_TIMESTAMP()*1000, 'N'),
('root-vegetables', 2, 'Root Vegetables', 'root.png', UNIX_TIMESTAMP()*1000, 'N'),
('seasonal-fruits', 2, 'Seasonal Fruits', 'seasonal_fruits.png', UNIX_TIMESTAMP()*1000, 'N'),

-- Dairy Products
('milk-products', 3, 'Milk Products', 'milk.png', UNIX_TIMESTAMP()*1000, 'N'),
('curd-yogurt', 3, 'Curd & Yogurt', 'curd.png', UNIX_TIMESTAMP()*1000, 'N'),
('paneer', 3, 'Paneer', 'paneer.png', UNIX_TIMESTAMP()*1000, 'N'),
('ghee', 3, 'Ghee', 'ghee.png', UNIX_TIMESTAMP()*1000, 'N'),

-- Spices & Masala
('whole-spices', 4, 'Whole Spices', 'whole_spices.png', UNIX_TIMESTAMP()*1000, 'N'),
('powdered-masala', 4, 'Powdered Masala', 'masala.png', UNIX_TIMESTAMP()*1000, 'N'),
('blended-masala', 4, 'Blended Masala', 'blended.png', UNIX_TIMESTAMP()*1000, 'N'),
('regional-masala', 4, 'Regional Masala', 'regional.png', UNIX_TIMESTAMP()*1000, 'N'),
('herbs-seasoning', 4, 'Herbs & Seasoning', 'herbs.png', UNIX_TIMESTAMP()*1000, 'N'),

-- Grains & Pulses
('rice-varieties', 5, 'Rice Varieties', 'rice.png', UNIX_TIMESTAMP()*1000, 'N'),
('dal-pulses', 5, 'Dal & Pulses', 'dal.png', UNIX_TIMESTAMP()*1000, 'N'),
('wheat-flour', 5, 'Wheat & Flour', 'wheat.png', UNIX_TIMESTAMP()*1000, 'N'),
('millets', 5, 'Millets', 'millets.png', UNIX_TIMESTAMP()*1000, 'N'),
('organic-grains', 5, 'Organic Grains', 'organic.png', UNIX_TIMESTAMP()*1000, 'N'),

-- Snacks & Namkeen
('namkeen', 6, 'Namkeen', 'namkeen.png', UNIX_TIMESTAMP()*1000, 'N'),
('biscuits', 6, 'Biscuits', 'biscuits.png', UNIX_TIMESTAMP()*1000, 'N'),
('chips', 6, 'Chips', 'chips.png', UNIX_TIMESTAMP()*1000, 'N'),
('traditional-snacks', 6, 'Traditional Snacks', 'traditional.png', UNIX_TIMESTAMP()*1000, 'N'),
('ready-to-eat', 6, 'Ready To Eat', 'ready.png', UNIX_TIMESTAMP()*1000, 'N'),

-- Beverages
('soft-drinks', 7, 'Soft Drinks', 'soft_drinks.png', UNIX_TIMESTAMP()*1000, 'N'),
('tea-coffee', 7, 'Tea & Coffee', 'tea.png', UNIX_TIMESTAMP()*1000, 'N'),
('fruit-juices', 7, 'Fruit Juices', 'juice.png', UNIX_TIMESTAMP()*1000, 'N'),
('energy-drinks', 7, 'Energy Drinks', 'energy.png', UNIX_TIMESTAMP()*1000, 'N'),
('health-drinks', 7, 'Health Drinks', 'health.png', UNIX_TIMESTAMP()*1000, 'N'),

-- Bakery Items
('bread', 8, 'Bread', 'bread.png', UNIX_TIMESTAMP()*1000, 'N'),
('cakes-pastries', 8, 'Cakes & Pastries', 'cakes.png', UNIX_TIMESTAMP()*1000, 'N'),
('cookies', 8, 'Cookies', 'cookies.png', UNIX_TIMESTAMP()*1000, 'N'),
('rusk', 8, 'Rusk', 'rusk.png', UNIX_TIMESTAMP()*1000, 'N'),
('buns', 8, 'Buns', 'buns.png', UNIX_TIMESTAMP()*1000, 'N'),

-- Personal Care
('skin-care', 9, 'Skin Care', 'skin.png', UNIX_TIMESTAMP()*1000, 'N'),
('hair-care', 9, 'Hair Care', 'hair.png', UNIX_TIMESTAMP()*1000, 'N'),
('oral-care', 9, 'Oral Care', 'oral.png', UNIX_TIMESTAMP()*1000, 'N'),
('men-grooming', 9, 'Men Grooming', 'men.png', UNIX_TIMESTAMP()*1000, 'N'),
('women-hygiene', 9, 'Women Hygiene', 'women.png', UNIX_TIMESTAMP()*1000, 'N'),

-- Household Items
('cleaning-supplies', 10, 'Cleaning Supplies', 'cleaning.png', UNIX_TIMESTAMP()*1000, 'N'),
('kitchen-essentials', 10, 'Kitchen Essentials', 'kitchen.png', UNIX_TIMESTAMP()*1000, 'N'),
('laundry-care', 10, 'Laundry Care', 'laundry.png', UNIX_TIMESTAMP()*1000, 'N'),
('bathroom-cleaners', 10, 'Bathroom Cleaners', 'bathroom.png', UNIX_TIMESTAMP()*1000, 'N'),
('disposables', 10, 'Disposables', 'disposable.png', UNIX_TIMESTAMP()*1000, 'N')

ON DUPLICATE KEY
UPDATE slug = slug;
