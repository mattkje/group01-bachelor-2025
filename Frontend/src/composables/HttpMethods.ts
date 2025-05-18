/**
 * This file contains functions to handle HTTP requests using the Fetch API.
 */

/**
 * PUT-request to update data on the server.
 *
 * @param url the URL to send the request to
 * @param body the data to be sent in the request body
 */
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

/**
 * GET-request to fetch data from the server.
 *
 * @param url the URL to send the request to
 */
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

/**
 * POST-request to send data to the server.
 *
 * @param url the URL to send the request to
 * @param body the data to be sent in the request body
 */
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

/**
 * POST-request to send data to the server with URL parameters.
 *
 * @param url the URL to send the request to
 * @param params the parameters to be sent in the URL
 */
export const postWithParams = async (url: string, params: Record<string, any>): Promise<void> => {
    try {
        const queryString = new URLSearchParams(params).toString();
        const fullUrl = `${url}?${queryString}`;
        const response = await fetch(fullUrl, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
        });
        if (!response.ok) {
            throw new Error(`Failed to POST to ${fullUrl}: ${response.statusText}`);
        }
    } catch (error) {
        console.error(`Error in POST request to ${url}:`, error);
        throw error;
    }
}

/**
 * DELETE-request to remove data from the server.
 *
 * @param url the URL to send the request to
 */
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

/**
 * Function to run a command on the server.
 *
 * @param url the URL to send the request to
 */
export const runCommand = async (url: string) => {
    try {
        await fetch(url);
    } catch (error) {
        console.error(`Error fetching data from ${url}:`, error);
        throw error;
    }
};