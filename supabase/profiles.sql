create table if not exists public.profiles (
    id                 uuid primary key references auth.users (id) on delete cascade,
    email              text not null,
    last_name          text not null,
    first_name         text not null,
    middle_name        text,
    birth_date         date,
    gender             text,
    license_number     text,
    license_issue_date date,
    created_at         timestamptz not null default now()
);

alter table public.profiles enable row level security;
grant select, update on public.profiles to authenticated;

drop policy if exists "profiles_select_own" on public.profiles;
create policy "profiles_select_own" on public.profiles
    for select to authenticated using (auth.uid() = id);

drop policy if exists "profiles_update_own" on public.profiles;
create policy "profiles_update_own" on public.profiles
    for update to authenticated using (auth.uid() = id) with check (auth.uid() = id);

create or replace function public.handle_new_user()
returns trigger
language plpgsql
security definer
set search_path = ''
as $$
declare
    meta jsonb := coalesce(new.raw_user_meta_data, '{}'::jsonb);
begin
    insert into public.profiles (
        id, email, last_name, first_name, middle_name,
        birth_date, gender, license_number, license_issue_date
    ) values (
        new.id,
        new.email,
        coalesce(meta ->> 'last_name', ''),
        coalesce(meta ->> 'first_name', ''),
        nullif(meta ->> 'middle_name', ''),
        to_date(nullif(meta ->> 'birth_date', ''), 'DD/MM/YYYY'),
        nullif(meta ->> 'gender', ''),
        nullif(meta ->> 'license_number', ''),
        to_date(nullif(meta ->> 'license_issue_date', ''), 'DD/MM/YYYY')
    );
    return new;
end;
$$;

drop trigger if exists on_auth_user_created on auth.users;
create trigger on_auth_user_created
    after insert on auth.users
    for each row execute function public.handle_new_user();
