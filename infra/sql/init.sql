IF DB_ID(N'$(AppDatabase)') IS NULL
BEGIN
    DECLARE @createDatabase nvarchar(max) = N'CREATE DATABASE [' + REPLACE('$(AppDatabase)', ']', ']]') + N']';
    EXEC (@createDatabase);
END;
GO

IF SUSER_ID(N'$(AppLogin)') IS NULL
BEGIN
    DECLARE @createLogin nvarchar(max) = N'CREATE LOGIN [' + REPLACE('$(AppLogin)', ']', ']]')
        + N'] WITH PASSWORD = N''' + REPLACE('$(AppPassword)', '''', '''''') + N''', CHECK_POLICY = ON';
    EXEC (@createLogin);
END;
GO

USE [$(AppDatabase)];
GO

IF DATABASE_PRINCIPAL_ID(N'$(AppLogin)') IS NULL
BEGIN
    DECLARE @createUser nvarchar(max) = N'CREATE USER [' + REPLACE('$(AppLogin)', ']', ']]')
        + N'] FOR LOGIN [' + REPLACE('$(AppLogin)', ']', ']]') + N']';
    EXEC (@createUser);
    ALTER ROLE db_owner ADD MEMBER [$(AppLogin)];
END;
GO
