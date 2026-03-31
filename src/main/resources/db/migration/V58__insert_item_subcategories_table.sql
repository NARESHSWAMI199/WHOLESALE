INSERT
IGNORE INTO item_subcategories
(slug, category_id, subcategory, unit, icon, updated_at, is_deleted) VALUES

('other',0,'Other',NULL,NULL,UNIX_TIMESTAMP()*1000,'N'),
-- Groceries (1)
('rice',1,'Rice','kg',NULL,UNIX_TIMESTAMP()*1000,'N'),
('wheat-flour',1,'Wheat Flour','kg',NULL,UNIX_TIMESTAMP()*1000,'N'),
('pulses',1,'Pulses','kg',NULL,UNIX_TIMESTAMP()*1000,'N'),
('spices',1,'Spices','pack',NULL,UNIX_TIMESTAMP()*1000,'N'),
('cooking-oil',1,'Cooking Oil','ltr',NULL,UNIX_TIMESTAMP()*1000,'N'),

-- Fruits & Vegetables (2)
('fresh-fruits',2,'Fresh Fruits','kg',NULL,UNIX_TIMESTAMP()*1000,'N'),
('fresh-vegetables',2,'Fresh Vegetables','kg',NULL,UNIX_TIMESTAMP()*1000,'N'),
('leafy-vegetables',2,'Leafy Vegetables','kg',NULL,UNIX_TIMESTAMP()*1000,'N'),
('exotic-vegetables',2,'Exotic Vegetables','kg',NULL,UNIX_TIMESTAMP()*1000,'N'),
('cut-vegetables',2,'Cut Vegetables','pack',NULL,UNIX_TIMESTAMP()*1000,'N'),

-- Dairy & Bakery (3)
('milk',3,'Milk','ltr',NULL,UNIX_TIMESTAMP()*1000,'N'),
('curd',3,'Curd','kg',NULL,UNIX_TIMESTAMP()*1000,'N'),
('cheese',3,'Cheese','pack',NULL,UNIX_TIMESTAMP()*1000,'N'),
('bread',3,'Bread','pack',NULL,UNIX_TIMESTAMP()*1000,'N'),
('butter',3,'Butter','pack',NULL,UNIX_TIMESTAMP()*1000,'N'),

-- Beverages (4)
('soft-drinks',4,'Soft Drinks','ltr',NULL,UNIX_TIMESTAMP()*1000,'N'),
('fruit-juices',4,'Fruit Juices','ltr',NULL,UNIX_TIMESTAMP()*1000,'N'),
('tea',4,'Tea','pack',NULL,UNIX_TIMESTAMP()*1000,'N'),
('coffee',4,'Coffee','pack',NULL,UNIX_TIMESTAMP()*1000,'N'),
('energy-drinks',4,'Energy Drinks','can',NULL,UNIX_TIMESTAMP()*1000,'N'),

-- Snacks (5)
('chips',5,'Chips','pack',NULL,UNIX_TIMESTAMP()*1000,'N'),
('biscuits',5,'Biscuits','pack',NULL,UNIX_TIMESTAMP()*1000,'N'),
('namkeen',5,'Namkeen','pack',NULL,UNIX_TIMESTAMP()*1000,'N'),
('chocolates',5,'Chocolates','pack',NULL,UNIX_TIMESTAMP()*1000,'N'),
('instant-noodles',5,'Instant Noodles','pack',NULL,UNIX_TIMESTAMP()*1000,'N'),

-- Personal Care (6)
('soap',6,'Soap','pcs',NULL,UNIX_TIMESTAMP()*1000,'N'),
('shampoo',6,'Shampoo','bottle',NULL,UNIX_TIMESTAMP()*1000,'N'),
('toothpaste',6,'Toothpaste','tube',NULL,UNIX_TIMESTAMP()*1000,'N'),
('hair-oil',6,'Hair Oil','bottle',NULL,UNIX_TIMESTAMP()*1000,'N'),
('skin-care',6,'Skin Care','pack',NULL,UNIX_TIMESTAMP()*1000,'N'),

-- Household Items (7)
('cleaning-liquids',7,'Cleaning Liquids','bottle',NULL,UNIX_TIMESTAMP()*1000,'N'),
('detergents',7,'Detergents','kg',NULL,UNIX_TIMESTAMP()*1000,'N'),
('dishwash',7,'Dishwash','bottle',NULL,UNIX_TIMESTAMP()*1000,'N'),
('toilet-cleaner',7,'Toilet Cleaner','bottle',NULL,UNIX_TIMESTAMP()*1000,'N'),
('room-freshener',7,'Room Freshener','can',NULL,UNIX_TIMESTAMP()*1000,'N'),

-- Meat & Seafood (8)
('chicken',8,'Chicken','kg',NULL,UNIX_TIMESTAMP()*1000,'N'),
('mutton',8,'Mutton','kg',NULL,UNIX_TIMESTAMP()*1000,'N'),
('fish',8,'Fish','kg',NULL,UNIX_TIMESTAMP()*1000,'N'),
('prawns',8,'Prawns','kg',NULL,UNIX_TIMESTAMP()*1000,'N'),
('frozen-meat',8,'Frozen Meat','pack',NULL,UNIX_TIMESTAMP()*1000,'N'),

-- Baby Care (9)
('baby-food',9,'Baby Food','pack',NULL,UNIX_TIMESTAMP()*1000,'N'),
('baby-diapers',9,'Baby Diapers','pack',NULL,UNIX_TIMESTAMP()*1000,'N'),
('baby-soap',9,'Baby Soap','pcs',NULL,UNIX_TIMESTAMP()*1000,'N'),
('baby-oil',9,'Baby Oil','bottle',NULL,UNIX_TIMESTAMP()*1000,'N'),
('baby-wipes',9,'Baby Wipes','pack',NULL,UNIX_TIMESTAMP()*1000,'N'),

-- Health & Wellness (10)
('vitamins',10,'Vitamins','pack',NULL,UNIX_TIMESTAMP()*1000,'N'),
('protein-supplements',10,'Protein Supplements','jar',NULL,UNIX_TIMESTAMP()*1000,'N'),
('ayurvedic',10,'Ayurvedic Products','pack',NULL,UNIX_TIMESTAMP()*1000,'N'),
('medical-devices',10,'Medical Devices','pcs',NULL,UNIX_TIMESTAMP()*1000,'N'),
('first-aid',10,'First Aid','kit',NULL,UNIX_TIMESTAMP()*1000,'N');
