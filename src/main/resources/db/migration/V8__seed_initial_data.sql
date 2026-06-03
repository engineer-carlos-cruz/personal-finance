-- V8__seed_initial_data.sql
--
-- Seed reference data for accounts, income categories, and expense categories.

-- Deployment considerations:
--   - Safe to run after V1–V7 have been applied.
--   - Uses ON CONFLICT so it is idempotent if ever re-applied manually.

-- Accounts

INSERT INTO accounts (code, description)
VALUES
    ('BILLETERA',       'Billetera'),
    ('DAVIVIENDA',      'Davivienda (cuenta bancaria de nómina)'),
    ('CCF_COMPENSAR',   'CCF Compensar')
ON CONFLICT (code) DO NOTHING;

-- Income categories

INSERT INTO income_categories (name, description)
VALUES
    ('Sueldo Longport', 'Ingreso salarial de Longport'),
    ('Subsidio CCF',    'Subsidio de la Caja de Compensación'),
    ('Otros',           'Otros ingresos')
ON CONFLICT DO NOTHING;

-- Expense categories

INSERT INTO expense_categories (name)
VALUES
    ('Servicio de administración conjunto Milano'),
    ('Servicio de internet y TV'),
    ('Anticipo seguro funerario'),
    ('Servicio de agua'),
    ('Seguro VantiMax'),
    ('Anticipo declaración de renta'),
    ('Anticipo impuesto predial'),
    ('Hipotéca FNA'),
    ('Alimentación y aseo'),
    ('Onces Juan Esteban'),
    ('Cuota Madre'),
    ('Ahorro para entretenimiento'),
    ('Ahorro para obsequios'),
    ('Gastos imprevistos'),
    ('Otras deudas')
ON CONFLICT DO NOTHING;
