export const putData = async (url: string, body?: any): Promise<void> => {
    try {
        const response = await fetch(url, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
            },
            body: body ? JSON.stringify(body) : undefined,
        });
        if (!response.ok) {
            throw new Error(`Failed to PUT to ${url}: ${response.statusText}`);
        }
    } catch (error) {
        console.error(`Error in PUT request to ${url}:`, error);
        throw error;
    }
};

export const fetchData = async <T>(url: string): Promise<T> => {
    try {
        const response = await fetch(url);
        if (!response.ok) {
            throw new Error(`Failed to fetch from ${url}: ${response.statusText}`);
        }
        return await response.json();
    } catch (error) {
        console.error(`Error fetching data from ${url}:`, error);
        throw error;
    }
};

export const postData = async (url: string, body?: any): Promise<void> => {
    try {
        const response = await fetch(url, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: body ? JSON.stringify(body) : undefined,
        });
        if (!response.ok) {
            throw new Error(`Failed to POST to ${url}: ${response.statusText}`);
        }
    } catch (error) {
        console.error(`Error in POST request to ${url}:`, error);
        throw error;
    }
};

export const deleteData = async (url: string): Promise<void> => {
    try {
        const response = await fetch(url, {
            method: 'DELETE',
        });
        if (!response.ok) {
            throw new Error(`Failed to DELETE from ${url}: ${response.statusText}`);
        }
    } catch (error) {
        console.error(`Error in DELETE request to ${url}:`, error);
        throw error;
    }
};