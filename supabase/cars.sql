-- Лабораторная работа №3: каталог автомобилей и аватар пользователя.
-- Выполнить целиком в Supabase → SQL Editor (после profiles.sql).

-- 1. Таблица автомобилей ------------------------------------------------------

create table if not exists public.cars (
    id            bigint generated always as identity primary key,
    brand         text    not null,             -- марка: Mercedes-Benz
    model         text    not null,             -- модель: S 500 Sedan
    price_per_day integer not null check (price_per_day > 0),
    transmission  text    not null default 'А/Т',
    fuel_type     text    not null default 'Бензин',
    image_url     text,
    is_available  boolean not null default true,
    created_at    timestamptz not null default now()
);

alter table public.cars enable row level security;
grant select on public.cars to anon, authenticated;

drop policy if exists "cars_select_all" on public.cars;
create policy "cars_select_all" on public.cars
    for select to anon, authenticated using (true);

-- Тестовые данные. Фото — миниатюры с Wikimedia Commons (свободная лицензия).
-- Можно заменить на ссылки из своего бакета Supabase Storage.
insert into public.cars (brand, model, price_per_day, transmission, fuel_type, image_url)
select * from (values
    ('Mercedes-Benz', 'S 500 Sedan', 2500, 'А/Т', 'Бензин',
     'https://thumb.wikimedia.org/wikipedia/commons/thumb/2/2b/Mercedes-Benz_W223_S_500_4MATIC_Black_%281%29.jpg/960px-Mercedes-Benz_W223_S_500_4MATIC_Black_%281%29.jpg'),
    ('BMW', '520d', 2200, 'А/Т', 'Дизель',
     'https://thumb.wikimedia.org/wikipedia/commons/thumb/b/bf/BMW_520d_G30_2018.jpg/960px-BMW_520d_G30_2018.jpg'),
    ('Audi', 'A6 Allroad', 2300, 'А/Т', 'Дизель',
     'https://thumb.wikimedia.org/wikipedia/commons/thumb/e/e8/Audi_A6_Allroad_Quattro_C8_IMG_0088.jpg/960px-Audi_A6_Allroad_Quattro_C8_IMG_0088.jpg'),
    ('Toyota', 'Camry', 1800, 'А/Т', 'Бензин',
     'https://thumb.wikimedia.org/wikipedia/commons/thumb/e/ef/Toyota_Camry_%28XV70%29_IMG_9081.jpg/960px-Toyota_Camry_%28XV70%29_IMG_9081.jpg'),
    ('Kia', 'K5', 1600, 'А/Т', 'Бензин',
     'https://thumb.wikimedia.org/wikipedia/commons/thumb/0/03/2021_Kia_K5_side_view.jpg/960px-2021_Kia_K5_side_view.jpg'),
    ('Škoda', 'Octavia Combi', 1400, 'А/Т', 'Бензин',
     'https://thumb.wikimedia.org/wikipedia/commons/thumb/8/8e/%C5%A0koda_Octavia_IV_Combi_Facelift_IMG_9434.jpg/960px-%C5%A0koda_Octavia_IV_Combi_Facelift_IMG_9434.jpg'),
    ('Volkswagen', 'Polo Sedan', 1200, 'М/Т', 'Бензин',
     'https://thumb.wikimedia.org/wikipedia/commons/thumb/d/d4/Volkswagen_Polo_sedan_2020_%28Russia%2C_front_view_3%29.png/960px-Volkswagen_Polo_sedan_2020_%28Russia%2C_front_view_3%29.png'),
    ('Hyundai', 'Solaris', 1100, 'М/Т', 'Бензин',
     'https://thumb.wikimedia.org/wikipedia/commons/thumb/8/8b/2014-2017_Hyundai_Solaris_Sedan_%28front%29.jpg/960px-2014-2017_Hyundai_Solaris_Sedan_%28front%29.jpg')
) as seed (brand, model, price_per_day, transmission, fuel_type, image_url)
where not exists (select 1 from public.cars);

-- 2. Аватар в профиле ----------------------------------------------------------

alter table public.profiles add column if not exists avatar_url text;

-- 3. Хранилище аватаров: публичный бакет, писать можно только в свою папку {user_id}/

insert into storage.buckets (id, name, public)
values ('avatars', 'avatars', true)
on conflict (id) do nothing;

drop policy if exists "avatars_insert_own" on storage.objects;
create policy "avatars_insert_own" on storage.objects
    for insert to authenticated
    with check (bucket_id = 'avatars' and (storage.foldername(name))[1] = auth.uid()::text);

drop policy if exists "avatars_update_own" on storage.objects;
create policy "avatars_update_own" on storage.objects
    for update to authenticated
    using (bucket_id = 'avatars' and (storage.foldername(name))[1] = auth.uid()::text);

drop policy if exists "avatars_select_all" on storage.objects;
create policy "avatars_select_all" on storage.objects
    for select to anon, authenticated
    using (bucket_id = 'avatars');
